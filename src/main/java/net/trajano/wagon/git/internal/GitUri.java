package net.trajano.wagon.git.internal;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Git URI. This provides the logic to extract the components from a URI. The
 * URI string is in the form
 * <code>git:gitSpecificUri?branchName#relativeDirectory</code>.
 */
public class GitUri {
    /**
     * Github pages host pattern.
     */
    private static final Pattern GITHUB_PAGES_HOST_PATTERN = Pattern
            .compile("([a-z]+)\\.github\\.io.?");

    /**
     * Github pages path pattern.
     */
    private static final Pattern GITHUB_PAGES_PATH_PATTERN = Pattern
            .compile("/([^/]+)(/.*)?");

    /**
     * Builds a GitUri from a GitHub Pages URL.  It performs a DNS lookup for
     * the CNAME if the host does not match {@link #GITHUB_PAGES_HOST_PATTERN}.
     * 
     * @param gitHubPagesUrl
     *            GitHub Pages URL
     * @return a GitURI based on the GitHubPages URL
     * @throws TextParseException
     */
    public static GitUri buildFromGithubPages(final String gitHubPagesUrl)
            throws TextParseException {
        final URI uri = URI.create(
                URI.create(gitHubPagesUrl).getSchemeSpecificPart()).normalize();
        final Matcher m = GITHUB_PAGES_HOST_PATTERN.matcher(uri.getHost());
        final String username;
        if (m.matches()) {
            username = m.group(1);
        } else {
            final String cnameHost = getCnameForHost(uri.getHost());
            final Matcher m2 = GITHUB_PAGES_HOST_PATTERN.matcher(cnameHost);
            if (!m2.matches()) {
                throw new RuntimeException("Invalid host for github pages "
                        + gitHubPagesUrl);
            }
            username = m2.group(1);
        }
        final Matcher pathMatcher = GITHUB_PAGES_PATH_PATTERN.matcher(uri
                .getPath());
        pathMatcher.matches();
        return new GitUri("ssh://git@github.com/" + username + "/"
                + pathMatcher.group(1) + ".git", "gh-pages", "");
    }

    private static String getCnameForHost(final String host)
            throws TextParseException {
        final Lookup lookup = new Lookup(host, Type.CNAME);
        lookup.run();
        if (lookup.getAnswers().length == 0) {
            return null;
        }
        return ((CNAMERecord) lookup.getAnswers()[0]).getTarget().toString();
    }

    /**
     * Branch name.
     */
    private final String branchName;

    /**
     * Repository URI.
     */
    private final String gitRepositoryUri;

    /**
     * Resource. This is the fragment part of the URI.
     */
    private final String resource;

    /**
     * Constructs the object from a URI string that contains "git:" schema.
     *
     * @param uriString
     *            URI string.
     * @throws URISyntaxException
     *             parsing exception
     */
    public GitUri(final String uriString) throws URISyntaxException {
        final URI uri = new URI(uriString.replace("##", "#"));
        final URI gitUri = new URI(uri.getSchemeSpecificPart());
        branchName = gitUri.getQuery();
        final String asciiUriString = gitUri.toASCIIString();
        gitRepositoryUri = asciiUriString.substring(0,
                asciiUriString.indexOf('?'));
        resource = uri.getFragment();
    }

    /**
     *
     * @param gitRepositoryUri
     *            git repository URL.
     * @param branchName
     *            branch name usually "gh-pages"
     * @param resource
     *            resource
     */
    private GitUri(final String gitRepositoryUri, final String branchName,
            final String resource) {
        this.gitRepositoryUri = gitRepositoryUri;
        this.branchName = branchName;
        this.resource = resource;
    }

    /**
     *
     * @param gitUri
     *            valid git URI (i.e. no git: schema).
     */
    private GitUri(final URI gitUri) {
        branchName = gitUri.getQuery();
        final String asciiUriString = gitUri.toASCIIString();
        gitRepositoryUri = asciiUriString.substring(0,
                asciiUriString.lastIndexOf('?'));
        resource = gitUri.getFragment();
    }

    /**
     * Branch name.
     *
     * @return branch name
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Git repository URI. This returns a string as that is what is being used
     * by the JGit API.
     *
     * @return Git repository URI
     */
    public String getGitRepositoryUri() {
        return gitRepositoryUri;
    }

    public String getResource() {
        return resource;
    }

    /**
     * Resolves a new {@link GitUri} with the fragment. The schema portion will
     * find the last occurrence of a URI path segment with a "?" character as
     * the Git URI with the branch name.
     *
     * @param fragment
     *            may contain escaped characters.
     * @return resolved {@link GitUri}
     */
    public GitUri resolve(final String fragment) {
        // TODO clean this up so it is less "hacky"
        final String decodedFragment;
        try {
            decodedFragment = URLDecoder.decode(fragment, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // should not happen.
            throw new IllegalStateException(e);
        }
        final URI combined;
        if (resource != null) {
            combined = URI.create(gitRepositoryUri
                    + resource.replace("##", "#"));
        } else {
            combined = URI.create(gitRepositoryUri);
        }
        final String slashAppendedUri = combined.toASCIIString();
        final URI resolvedUri = URI.create(slashAppendedUri).resolve(
                decodedFragment.replace(" ", "%2520"));
        final StringBuilder resolved = new StringBuilder(
                resolvedUri.toASCIIString());

        final int lastQuestionMark = resolved.lastIndexOf("?");
        if (lastQuestionMark == -1) {
            return new GitUri(URI.create(format("%s?%s#%s", gitRepositoryUri,
                    branchName, decodedFragment.replace(" ", "%20"))));
        }
        final int lastRelevantSlash = resolved.indexOf("/", lastQuestionMark);
        final int lastRelevantHash = resolved.indexOf("#", lastQuestionMark);

        if (lastRelevantSlash > 0 && lastRelevantHash >= lastRelevantSlash) {
            resolved.insert(lastRelevantSlash, '#');
        }

        return new GitUri(URI.create(resolved.toString()));
    }
}
