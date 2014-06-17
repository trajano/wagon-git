package net.trajano.wagon.git;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
@Component(role = Wagon.class, hint = "git", instantiationStrategy = "per-lookup")
public class GitWagon extends AbstractGitWagon {
    /**
     * Constructs the object from a URI string that contains "git:" schema.
     * {@inheritDoc}
     */
    @Override
    public GitUri buildGitUri(final URI gitUri) {
        final String branchName = gitUri.getQuery();
        final String asciiUriString = gitUri.toASCIIString();
        final String gitRepositoryUri = asciiUriString.substring(0,
                asciiUriString.indexOf('?'));
        final String resource = gitUri.getFragment();
        return new GitUri(gitRepositoryUri, branchName, resource);
    }

    @Override
    public File getFileForResource(final String resourceName)
            throws GitAPIException, IOException, URISyntaxException {
        // /foo/bar/foo.git + ../bar.git == /foo/bar/bar.git + /
        // /foo/bar/foo.git + ../bar.git/abc == /foo/bar/bar.git + /abc
        final GitUri resolved = getGitUri().resolve(resourceName);
        Git resourceGit;
        try {
            resourceGit = getGit(resolved.getGitRepositoryUri());
        } catch (final ResourceDoesNotExistException e) {
            return null;
        }

        final File workTree = resourceGit.getRepository().getWorkTree();
        final File resolvedFile = new File(workTree, resolved.getResource());
        if (!resolvedFile.getCanonicalPath().startsWith(
                workTree.getCanonicalPath())) {
            throw new IOException(String.format(
                    "The resolved file '%s' is not in work tree '%s'",
                    resolvedFile, workTree));
        }
        return resolvedFile;
    }
}
