package net.trajano.wagon.git;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Git URI. Provides component extraction. The URI string is in the form
 * git:[git specific uri]?branchname#ignored
 */
public class GitUri {
    /**
     * Branch name.
     */
    private final String branchName;

    /**
     * Repository URI.
     */
    private final String gitRepositoryUri;

    /**
     * Constructs the object from a URI string.
     * 
     * @param uriString
     *            URI string.
     * @throws URISyntaxException
     *             parsing exception
     */
    public GitUri(final String uriString) throws URISyntaxException {
        final URI uri = new URI(uriString);
        final URI gitUri = new URI(uri.getSchemeSpecificPart());
        branchName = gitUri.getQuery();
        gitRepositoryUri = gitUri.toASCIIString().substring(0,
                gitUri.toASCIIString().indexOf("?"));
    }

    public String getBranchName() {
        return branchName;
    }

    public String getGitRepositoryUri() {
        return gitRepositoryUri;
    }
}
