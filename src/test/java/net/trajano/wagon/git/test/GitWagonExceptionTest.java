package net.trajano.wagon.git.test;

import java.io.File;
import java.io.FileOutputStream;

import net.trajano.wagon.git.GitWagon;

import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests exception scenarios.
 *
 * @author Archimedes
 */
public class GitWagonExceptionTest {

    /**
     * Git remote directory 1.
     */
    private File gitRemoteDirectory1;

    /**
     * Git remote directory 2.
     */
    private File gitRemoteDirectory2;

    /**
     * Defines the {@link #gitRemoteDirectory1}.
     */
    @Before
    public void createRemote() throws Exception {

        gitRemoteDirectory1 = File.createTempFile("remote", null);
        gitRemoteDirectory1.delete();
        Git.init()
            .setDirectory(gitRemoteDirectory1)
            .call();
        gitRemoteDirectory2 = File.createTempFile("remote", null);
        gitRemoteDirectory2.delete();
        Git.init()
            .setDirectory(gitRemoteDirectory2)
            .call();
    }

    @Test(expected = TransferFailedException.class)
    public void testPutOutside() throws Exception {

        final GitWagon gitWagon = new GitWagon();
        gitWagon.connect(new Repository("gh", "git:" + gitRemoteDirectory1.toURI() + "?ghPages#"));
        final File tempDir = File.createTempFile("temp", null);
        tempDir.delete();
        tempDir.mkdir();
        new FileOutputStream(new File(tempDir, "foo")).close();
        new FileOutputStream(new File(tempDir, "bar")).close();
        new FileOutputStream(new File(tempDir, "one")).close();
        gitWagon.putDirectory(tempDir, "../");
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testPutParent() throws Exception {

        final GitWagon gitWagon = new GitWagon();
        gitWagon.connect(new Repository("gh", "git:" + gitRemoteDirectory1.toURI() + "?ghPages#"));
        final File tempDir = File.createTempFile("temp", null);
        tempDir.delete();
        tempDir.mkdir();
        new FileOutputStream(new File(tempDir, "foo")).close();
        new FileOutputStream(new File(tempDir, "bar")).close();
        new FileOutputStream(new File(tempDir, "one")).close();
        gitWagon.putDirectory(tempDir, "../" + gitRemoteDirectory2.getName() + "?ghPages#");
        FileUtils.deleteDirectory(tempDir);
    }

    @Test(expected = TransferFailedException.class)
    public void testUnableToCreateDirs() throws Exception {

        final GitWagon gitWagon = new GitWagon();
        gitWagon.connect(new Repository("gh", "git:" + gitRemoteDirectory1.toURI() + "?ghPages#"));
        final File temp = File.createTempFile("temp", null);
        gitWagon.put(temp, "////");
        temp.delete();
    }
}
