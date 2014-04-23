package net.trajano.wagon.git;

import java.net.URI;
import java.net.URISyntaxException;

import net.trajano.wagon.git.internal.AbstractGitWagon;
import net.trajano.wagon.git.internal.GitUri;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.codehaus.plexus.component.annotations.Component;

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
    public GitUri buildGitUri(final String repositoryUrl)
            throws ConnectionException, AuthenticationException {
        try {
            final URI uri = new URI(repositoryUrl.replace("##", "#"));
            final URI gitUri = new URI(uri.getSchemeSpecificPart());
            final String branchName = gitUri.getQuery();
            final String asciiUriString = gitUri.toASCIIString();
            final String gitRepositoryUri = asciiUriString.substring(0,
                    asciiUriString.indexOf('?'));
            final String resource = uri.getFragment();
            return new GitUri(gitRepositoryUri, branchName, resource);
        } catch (final URISyntaxException e) {
            throw new ConnectionException(e.getMessage());
        }
    }
}
