package net.trajano.wagon.git;

import net.trajano.wagon.git.internal.AbstractGitWagon;
import net.trajano.wagon.git.internal.GitUri;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.codehaus.plexus.component.annotations.Component;
import org.xbill.DNS.TextParseException;

/**
 * Github Pages Wagon.
 */
@Component(role = Wagon.class, hint = "github", instantiationStrategy = "per-lookup")
public class GitHubPagesWagon extends AbstractGitWagon {

    /**
     * Sets the initial git URI.
     */
    @Override
    protected void openConnectionInternal() throws ConnectionException,
            AuthenticationException {
        try {
            gitUri = GitUri.buildFromGithubPages(getRepository().getUrl());
        } catch (final TextParseException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

}
