package net.trajano.wagon.git;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import net.trajano.wagon.git.internal.AbstractGitWagon;
import net.trajano.wagon.git.internal.GitUri;

/**
 * Github Pages Wagon.
 */
@Component(role = Wagon.class, hint = "github", instantiationStrategy = "per-lookup")
public class GitHubPagesWagon extends AbstractGitWagon {

    /**
     * Github pages host pattern.
     */
    private static final Pattern GITHUB_PAGES_HOST_PATTERN = Pattern.compile("([a-z0-9-]+)\\.github\\.io.?");

    /**
     * Github pages path pattern.
     */
    private static final Pattern GITHUB_PAGES_PATH_PATTERN = Pattern.compile("/([^/]+)(/.*)?");

    /**
     * Logger.
     */
    private static final Logger LOG;

    /**
     * Messages resource path.
     */
    private static final String MESSAGES = "META-INF/Messages";

    /**
     * Resource bundle.
     */
    private static final ResourceBundle R;

    static {
        LOG = Logger.getLogger("net.trajano.wagon.git", MESSAGES);
        R = ResourceBundle.getBundle(MESSAGES);
    }

    /**
     * Builds a GitUri from a GitHub Pages URL. It performs a DNS lookup for the
     * CNAME if the host does not match {@link #GITHUB_PAGES_HOST_PATTERN}.
     * {@inheritDoc}
     */
    @Override
    public GitUri buildGitUri(final URI uri) throws IOException,
    URISyntaxException {

        final URI finalUri;
        // Resolve redirects if needed.
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            final HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL()
                    .openConnection();
            urlConnection.connect();
            urlConnection.getResponseCode();
            finalUri = urlConnection.getURL()
                    .toURI();
            urlConnection.disconnect();
        } else {
            finalUri = uri;
        }
        final Matcher m = GITHUB_PAGES_HOST_PATTERN.matcher(finalUri.getHost());
        final String username;
        if (m.matches()) {
            username = m.group(1);
        } else {
            final String cnameHost = getCnameForHost(finalUri.getHost());
            final Matcher m2 = GITHUB_PAGES_HOST_PATTERN.matcher(cnameHost);
            if (!m2.matches()) {
                throw new URISyntaxException(finalUri.toASCIIString(), String.format(R.getString("invalidGitHubPagesHost"), uri));
            }
            username = m2.group(1);
        }

        if ("".equals(finalUri.getPath()) || "/".equals(finalUri.getPath())) {
            return buildRootUri(username);
        } else {
            return buildProjectUri(username, finalUri.getPath());
        }
    }

    /**
     * Builds the project Github URI.
     *
     * @param username
     *            user name
     * @param path
     *            path
     * @return URI
     */
    private GitUri buildProjectUri(final String username,
            final String path) {

        final Matcher pathMatcher = GITHUB_PAGES_PATH_PATTERN.matcher(path);
        pathMatcher.matches();
        final String resource = pathMatcher.group(2);
        return new GitUri("ssh://git@github.com/" + username + "/" + pathMatcher.group(1) + ".git", "gh-pages", resource);
    }

    /**
     * Builds the user's Github URI.
     *
     * @param username
     *            user name
     * @return URI
     */
    private GitUri buildRootUri(final String username) {

        return new GitUri("ssh://git@github.com/" + username + "/" + username + ".github.io.git", "master", "/");
    }

    /**
     * Gets the CNAME record for the host.
     *
     * @param host
     *            host
     * @return CNAME record may return null if not found.
     * @throws TextParseException
     */
    private String getCnameForHost(final String host) throws TextParseException {

        final Lookup lookup = new Lookup(host, Type.CNAME);
        lookup.run();
        if (lookup.getAnswers().length == 0) {
            LOG.log(Level.SEVERE, "unableToFindCNAME", new Object[] { host });
            return null;
        }
        return ((CNAMERecord) lookup.getAnswers()[0]).getTarget()
                .toString();
    }

    /**
     * Does resolution a different way.
     *
     * @param resourceName
     *            resource name
     * @return file for the resource.
     * @throws ResourceDoesNotExistException
     */
    @Override
    public File getFileForResource(final String resourceName) throws GitAPIException,
    IOException,
    URISyntaxException {

        // /foo/bar/foo.git + ../bar.git == /foo/bar/bar.git + /
        // /foo/bar/foo.git + ../bar.git/abc == /foo/bar/bar.git + /abc
        final GitUri resolved = buildGitUri(URI.create(URI.create(getRepository().getUrl())
                .getSchemeSpecificPart())
                .resolve(resourceName.replace(" ", "%20")));
        Git resourceGit;
        try {
            resourceGit = getGit(resolved.getGitRepositoryUri());
        } catch (final ResourceDoesNotExistException e) {
            LOG.throwing(this.getClass()
                    .getName(), "getFileForResource", e);
            return null;
        }

        final File workTree = resourceGit.getRepository()
                .getWorkTree();
        final File resolvedFile = new File(workTree, resolved.getResource());
        if (!resolvedFile.getCanonicalPath()
                .startsWith(workTree.getCanonicalPath())) {
            throw new IOException(String.format("The resolved file '%s' is not in work tree '%s'", resolvedFile, workTree));
        }
        return resolvedFile;
    }
}
