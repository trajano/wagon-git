package net.trajano.wagon.git;

import java.net.URI;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.trajano.wagon.git.internal.AbstractGitWagon;
import net.trajano.wagon.git.internal.GitUri;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.codehaus.plexus.component.annotations.Component;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Github Pages Wagon.
 */
@Component(role = Wagon.class, hint = "github", instantiationStrategy = "per-lookup")
public class GitHubPagesWagon extends AbstractGitWagon {

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
    public GitUri buildGitUri(final String repositoryUrl)
            throws ConnectionException, AuthenticationException {
        try {
            final URI uri = URI.create(
                    URI.create(repositoryUrl).getSchemeSpecificPart())
                    .normalize();
            final Matcher m = GITHUB_PAGES_HOST_PATTERN.matcher(uri.getHost());
            final String username;
            if (m.matches()) {
                username = m.group(1);
            } else {
                final String cnameHost = getCnameForHost(uri.getHost());
                final Matcher m2 = GITHUB_PAGES_HOST_PATTERN.matcher(cnameHost);
                if (!m2.matches()) {
                    throw new RuntimeException(String.format(
                            R.getString("invalidGitHubPagesHost"),
                            repositoryUrl));
                }
                username = m2.group(1);
            }
            final Matcher pathMatcher = GITHUB_PAGES_PATH_PATTERN.matcher(uri
                    .getPath());
            pathMatcher.matches();
            return new GitUri("ssh://git@github.com/" + username + "/"
                    + pathMatcher.group(1) + ".git", "gh-pages", "");
        } catch (final TextParseException e) {
            throw new ConnectionException(e.getMessage());
        }
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
        return ((CNAMERecord) lookup.getAnswers()[0]).getTarget().toString();
    }
}
