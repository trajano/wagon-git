package net.trajano.wagon.git;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Git URI. This provides the logic to extract the components from a URI. The
 * URI string is in the form
 * <code>git:[git specific uri]?branchname#ignored</code>.
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
}
