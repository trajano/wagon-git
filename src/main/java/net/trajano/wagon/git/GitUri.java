package net.trajano.wagon.git;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Git URI. This provides the logic to extract the components from a URI. The
 * URI string is in the form
 * <code>git:gitSpecificUri?branchName#relativeDirectory</code>.
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
        final URI uri = new URI(uriString);
        final URI gitUri = new URI(uri.getSchemeSpecificPart());
        branchName = gitUri.getQuery();
        final String asciiUriString = gitUri.toASCIIString();
        gitRepositoryUri = asciiUriString.substring(0,
                asciiUriString.indexOf('?'));
        resource = uri.getFragment();
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
        final String decodedFragment;
        try {
            decodedFragment = URLDecoder.decode(fragment, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // should not happen.
            throw new IllegalStateException(e);
        }
        final URI combined;
        if (resource != null) {
            combined = URI.create(gitRepositoryUri + resource);
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

        if (lastRelevantSlash > 0) {
            resolved.insert(lastRelevantSlash, '#');
        }

        return new GitUri(URI.create(resolved.toString()));
    }
}
