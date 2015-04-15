package net.trajano.wagon.git.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.wagon.TransferFailedException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import net.trajano.wagon.git.internal.AbstractGitWagon;
import net.trajano.wagon.git.internal.GitUri;

public class BrokenGitProviderTest {

    @Test(expected = TransferFailedException.class)
    public void testGitApiException() throws Exception {

        new AbstractGitWagon() {

            @Override
            protected GitUri buildGitUri(final URI gitUri) throws IOException,
                    URISyntaxException {

                final String branchName = gitUri.getQuery();
                final String asciiUriString = gitUri.toASCIIString();
                final String gitRepositoryUri = asciiUriString.substring(0, asciiUriString.indexOf('?'));
                final String resource = gitUri.getFragment();
                return new GitUri(gitRepositoryUri, branchName, resource);
            }

            @Override
            protected File getFileForResource(final String resourceName) throws GitAPIException,
                    IOException,
                    URISyntaxException {

                throw new DetachedHeadException();
            }
        }.resourceExists("fail");
    }

    @Test(expected = TransferFailedException.class)
    public void testIOException() throws Exception {

        new AbstractGitWagon() {

            @Override
            protected GitUri buildGitUri(final URI gitUri) throws IOException,
                    URISyntaxException {

                final String branchName = gitUri.getQuery();
                final String asciiUriString = gitUri.toASCIIString();
                final String gitRepositoryUri = asciiUriString.substring(0, asciiUriString.indexOf('?'));
                final String resource = gitUri.getFragment();
                return new GitUri(gitRepositoryUri, branchName, resource);
            }

            @Override
            protected File getFileForResource(final String resourceName) throws GitAPIException,
                    IOException,
                    URISyntaxException {

                throw new IOException();
            }
        }.resourceExists("fail");
    }

    @Test(expected = TransferFailedException.class)
    public void testURISyntaxException() throws Exception {

        new AbstractGitWagon() {

            @Override
            protected GitUri buildGitUri(final URI gitUri) throws IOException,
                    URISyntaxException {

                final String branchName = gitUri.getQuery();
                final String asciiUriString = gitUri.toASCIIString();
                final String gitRepositoryUri = asciiUriString.substring(0, asciiUriString.indexOf('?'));
                final String resource = gitUri.getFragment();
                return new GitUri(gitRepositoryUri, branchName, resource);
            }

            @Override
            protected File getFileForResource(final String resourceName) throws GitAPIException,
                    IOException,
                    URISyntaxException {

                throw new URISyntaxException("bad", "input");
            }
        }.resourceExists("fail");
    }
}
