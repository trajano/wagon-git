package net.trajano.wagon.git.test;

import java.io.File;
import java.io.IOException;

import org.apache.maven.wagon.StreamingWagonTestCase;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RefUpdate;

public class GitWagonTest extends StreamingWagonTestCase {
    private File gitRemoteDirectory;

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
        Git git = Git.init().setDirectory(gitRemoteDirectory).call();
        File.createTempFile("temp", null, gitRemoteDirectory);
    }

    @Override
    protected void tearDownWagonTestingFixtures() throws Exception {
        FileUtils.deleteDirectory(gitRemoteDirectory);
    }

    @Override
    protected String getTestRepositoryUrl() throws IOException {
        return "git:" + gitRemoteDirectory.toURI() + "?gh-pages";
    }

    @Override
    protected String getProtocol() {
        return "git";
    }

    @Override
    protected int getTestRepositoryPort() {
        return 0;
    }

    @Override
    protected boolean supportsGetIfNewer() {
        return false;
    }

    @Override
    protected long getExpectedLastModifiedOnGet(Repository repository,
            Resource resource) {
        return new File(gitRemoteDirectory, resource.getName()).lastModified();
    }
}
