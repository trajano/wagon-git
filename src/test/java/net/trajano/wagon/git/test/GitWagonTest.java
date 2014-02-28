package net.trajano.wagon.git.test;

import java.io.File;
import java.io.IOException;

import net.trajano.wagon.git.GitWagon;

import org.apache.maven.wagon.StreamingWagonTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;

/**
 * Tests {@link GitWagon}.
 */
public class GitWagonTest extends StreamingWagonTestCase {
    /**
     * Git remote directory.
     */
    private File gitRemoteDirectory;

    /**
     * Protocol hint.
     * 
     * @return "git"
     */
    @Override
    protected String getProtocol() {
        return "git";
    }

    /**
     * Unused in the tests.
     */
    @Override
    protected int getTestRepositoryPort() {
        return 0;
    }

    /**
     * Gets the test repository URI based on {@link #gitRemoteDirectory}.
     */
    @Override
    protected String getTestRepositoryUrl() throws IOException {
        return "git:" + gitRemoteDirectory.toURI() + "?gh-pages";
    }

    /**
     * Defines the {@link #gitRemoteDirectory}.
     */
    @Override
    protected void setUp() throws Exception {
        gitRemoteDirectory = File.createTempFile("remote", null);
        gitRemoteDirectory.delete();
        super.setUp();
    }

    /**
     * Create a repository that has at least one commit.
     */
    @Override
    protected void setupWagonTestingFixtures() throws Exception {
        Git.init().setDirectory(gitRemoteDirectory).call();
        File.createTempFile("temp", null, gitRemoteDirectory);
    }

    /**
     * Unable to change how the timestamps are set, so getIfNewer is not
     * testable.
     */
    @Override
    protected boolean supportsGetIfNewer() {
        return false;
    }

    /**
     * Remove the "remote" directory.
     */
    @Override
    protected void tearDownWagonTestingFixtures() throws Exception {
        FileUtils.deleteDirectory(gitRemoteDirectory);
    }
}
