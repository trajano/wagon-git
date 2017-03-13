package net.trajano.wagon.git;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import net.trajano.wagon.git.internal.AbstractGitWagon;
import net.trajano.wagon.git.internal.GitUri;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Git Wagon. Due to issues with the way maven-site-plugin is improperly sending
 * requests that assume the target repository is a file system, the handling of
 * git URIs fails. This performs an inefficient, but working method of creating
 * a clone per request, but only once per Git repository.
 */
@Component(role = Wagon.class,
    hint = "git",
    instantiationStrategy = "per-lookup")
public class GitWagon extends AbstractGitWagon {

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
     * Constructs the object from a URI string that contains "git:" schema.
     * {@inheritDoc}
     * 
     * @param nonNormalizedGitUri
     *            a URI that is not yet normalized.
     */
    @Override
    public GitUri buildGitUri(final URI nonNormalizedGitUri) {

        final URI gitUri = nonNormalizedGitUri.normalize();
        final String branchName = gitUri.getQuery();
        final String asciiUriString = gitUri.toASCIIString();
        final String gitRepositoryUri = asciiUriString.substring(0, asciiUriString.indexOf('?'));
        final String resource = gitUri.getFragment();
        return new GitUri(gitRepositoryUri, branchName, resource);
    }

    @Override
    public File getFileForResource(final String resourceName) throws GitAPIException,
        IOException,
        URISyntaxException {

        // /foo/bar/foo.git + ../bar.git == /foo/bar/bar.git + /
        // /foo/bar/foo.git + ../bar.git/abc == /foo/bar/bar.git + /abc
        final GitUri resolved = getGitUri().resolve(resourceName);
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
            throw new IOException(String.format(R.getString("notInWorkTree"), resolvedFile, workTree));
        }
        return resolvedFile;
    }
}
