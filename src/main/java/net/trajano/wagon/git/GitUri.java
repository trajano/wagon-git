package net.trajano.wagon.git;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Git URI. Provides component extraction. The URI string is in the form
 * git:[git specific uri]?branchname#ignored
 */
public class GitUri {
    private final String branchName;

    public String getBranchName() {
        return branchName;
    }

    public GitUri(final String uriString) throws URISyntaxException {
        final URI uri = new URI(uriString);
        final URI gitUri = new URI(uri.getSchemeSpecificPart());
        branchName = gitUri.getQuery();
        gitRepositoryUri = gitUri.toASCIIString().substring(0,
                gitUri.toASCIIString().indexOf("?"));
    }

    public String getGitRepositoryUri() {
        return gitRepositoryUri;
    }

    private final String gitRepositoryUri;
}
