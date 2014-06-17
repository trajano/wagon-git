package net.trajano.wagon.git.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import net.trajano.wagon.git.GitHubPagesWagon;
import net.trajano.wagon.git.GitWagon;
import net.trajano.wagon.git.internal.GitUri;

import org.junit.Ignore;
import org.junit.Test;

public class GitUriTest {
    @Test
    public void testBasicGitUri() throws Exception {
        final GitUri gitUri = new GitWagon().buildGitUri(URI
                .create("ssh://github.com/trajano/trajano.git?gh-pages#/"));
        assertEquals("gh-pages", gitUri.getBranchName());
        assertEquals("ssh://github.com/trajano/trajano.git",
                gitUri.getGitRepositoryUri());
        assertEquals("/", gitUri.getResource());
    }

    @Test
    public void testBasicGitUriResolve() throws Exception {
        final GitUri gitUri = new GitWagon().buildGitUri(URI
                .create("ssh://github.com/trajano/trajano.git?gh-pages#/"));
        final GitUri resolved = gitUri
                .resolve("../coding-standards.git%3Fgh-pages%23");
        assertNotNull(resolved.getBranchName());
        assertEquals("gh-pages", resolved.getBranchName());
        assertEquals("ssh://github.com/trajano/coding-standards.git",
                resolved.getGitRepositoryUri());
        assertEquals("/", gitUri.getResource());
    }

    /**
     * Don't do this test until we have a confirmed case where a pattern is
     * executed.
     *
     * @throws Exception
     */
    @Ignore
    public void testBasicGitUriResolve2() throws Exception {
        final GitUri gitUri = new GitWagon().buildGitUri(URI
                .create("ssh://github.com/trajano/trajano.git?gh-pages#/"));
        final GitUri resolved = gitUri
                .resolve("../coding-standards.git%3Fgh-pages%23/foo");
        assertEquals("gh-pages", resolved.getBranchName());
        assertEquals("ssh://github.com/trajano/coding-standards.git",
                resolved.getGitRepositoryUri());
        assertEquals("/foo", gitUri.getResource());
    }

    @Test
    public void testGitHubPageEquivalence() throws Exception {
        final GitUri gitHubUri = new GitHubPagesWagon().buildGitUri(URI
                .create("http://site.trajano.net/trajano"));
        final GitUri gitUri = new GitWagon().buildGitUri(URI
                .create("ssh://git@github.com/trajano/trajano.git?gh-pages#/"));
        assertEquals(gitUri, gitHubUri);
    }

    @Test
    public void testGitHubPageEquivalence2() throws Exception {
        final GitUri gitHubUri = new GitHubPagesWagon().buildGitUri(URI
                .create("http://site.trajano.net/github/trajano"));
        final GitUri gitUri = new GitWagon()
                .buildGitUri(URI
                        .create("ssh://git@github.com/trajano/github.git?gh-pages#/trajano"));
        assertEquals(gitUri, gitHubUri);
    }

    @Test
    public void testGitHubPageEquivalence3() throws Exception {
        final GitUri gitHubUri = new GitHubPagesWagon().buildGitUri(URI
                .create("http://site.trajano.net/github/../trajano"));
        final GitUri gitUri = new GitWagon().buildGitUri(URI
                .create("ssh://git@github.com/trajano/trajano.git?gh-pages#/"));
        assertEquals(gitUri, gitHubUri);

    }

    @Test
    public void testGitHubPageRoot() throws Exception {
        final GitUri gitHubUri = new GitHubPagesWagon().buildGitUri(URI
                .create("http://site.trajano.net"));
        final GitUri gitUri = new GitWagon()
                .buildGitUri(URI
                .create("ssh://git@github.com/trajano/trajano.github.io.git?master#/"));
        assertEquals(gitUri, gitHubUri);
    }
}
