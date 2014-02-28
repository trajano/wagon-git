package net.trajano.wagon.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Git Wagon.
 */
@Component(role = Wagon.class, hint = "git", instantiationStrategy = "per-lookup")
public class GitWagon extends StreamWagon {

    /**
     * Git.
     */
    private Git git;

    /**
     * Local copy location.
     */
    private File gitDir;

    /**
     * This will commit the local changes and push them to the repository. If
     * the method is unable to push to the repository without force, it will
     * throw an exception. {@inheritDoc}
     */
    @Override
    public void closeConnection() throws ConnectionException {
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("wagon-git commit").call();
            git.push().setRemote(gitUri.getGitRepositoryUri()).call();
        } catch (final GitAPIException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    /**
     * This will read from the working copy. File modification date would not be
     * available as it does not really have any meaningful value.{@inheritDoc}
     */
    @Override
    public void fillInputData(final InputData inputData)
            throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
        try {
            final File file = new File(gitDir, inputData.getResource()
                    .getName());
            if (!file.exists()) {
                throw new ResourceDoesNotExistException("The resource " + file
                        + " does not exist.");
            }
            inputData.setInputStream(new FileInputStream(file));
            inputData.getResource().setContentLength(file.length());
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    /**
     * This will write to the working copy. {@inheritDoc}
     */
    @Override
    public void fillOutputData(final OutputData outputData)
            throws TransferFailedException {
        try {
            final File file = new File(gitDir, outputData.getResource()
                    .getName());
            file.getParentFile().mkdirs();
            outputData.setOutputStream(new FileOutputStream(file));
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getFileList(final String destinationDirectory)
            throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
        final File dir = new File(gitDir, destinationDirectory);
        final File[] files = dir.listFiles();
        if (files == null) {
            throw new ResourceDoesNotExistException("The directory "
                    + destinationDirectory + " does not exist");
        }
        final List<String> list = new LinkedList<String>();
        for (final File file : files) {
            String name = file.getName();
            if (file.isDirectory() && !name.endsWith("/")) {
                name += "/"; // NOPMD this is easier to read.
            }
            list.add(name);
        }
        return list;
    }

    /**
     * This will create or refresh the working copy. If the working copy cannot
     * be pulled cleanly this method will fail. {@inheritDoc}
     */
    @Override
    protected void openConnectionInternal() throws ConnectionException,
            AuthenticationException {
        try {
            gitUri = new GitUri(getRepository().getUrl());
            gitDir = File.createTempFile("wagon-git", null);
            gitDir.delete();
            gitDir.mkdir();

            git = Git.cloneRepository().setURI(gitUri.getGitRepositoryUri())
                    .setBranch(gitUri.getBranchName()).setDirectory(gitDir)
                    .call();
            if (!gitUri.getBranchName().equals(git.getRepository().getBranch())) {
                throw new ConnectionException("the branch "
                        + gitUri.getBranchName() + " does not exist in "
                        + gitUri.getGitRepositoryUri());
            }
        } catch (final GitAPIException e) {
            throw new ConnectionException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new ConnectionException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    /**
     * Git URI.
     */
    private GitUri gitUri;

    @Override
    public void putDirectory(final File sourceDirectory,
            final String destinationDirectory) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        try {
            FileUtils.copyDirectoryStructure(sourceDirectory, new File(gitDir,
                    destinationDirectory));
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    @Override
    public boolean resourceExists(final String resourceName)
            throws TransferFailedException, AuthorizationException {
        final File file = new File(gitDir, resourceName);

        if (resourceName.endsWith("/")) {
            return file.isDirectory();
        }

        return file.exists();
    }

    @Override
    public boolean supportsDirectoryCopy() {
        return true;
    }
}
