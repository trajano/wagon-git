package net.trajano.wagon.git.internal;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
     * Constructs the URI based on the parts.
     *
     * @param gitRepositoryUri
     *            git repository URL.
     * @param branchName
     *            branch name usually "gh-pages"
     * @param resource
     *            resource
     */
    public GitUri(final String gitRepositoryUri, final String branchName,
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

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final GitUri x = (GitUri) obj;
        return new EqualsBuilder().append(branchName, x.branchName)
                .append(gitRepositoryUri, x.gitRepositoryUri)
                .append(resource, x.resource).build();
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resource).append(branchName)
                .append(gitRepositoryUri).build();
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(gitRepositoryUri)
                .append(branchName).append(resource).build();
    }
}
