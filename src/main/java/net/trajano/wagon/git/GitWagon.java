package net.trajano.wagon.git;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Git Wagon.
 */
@Component(role = Wagon.class, hint = "git", instantiationStrategy = "per-lookup")
public class GitWagon extends StreamWagon {

    /**
     * Resource bundle.
     */
    private static final ResourceBundle R = ResourceBundle
            .getBundle("META-INF/Messages"); //$NON-NLS-1$

    /**
     * Credentials provider.
     */
    private UsernamePasswordCredentialsProvider credentialsProvider;

    /**
     * Git.
     */
    private Git git;

    /**
     * Local copy location.
     */
    private File gitDir;

    /**
     * Git URI.
     */
    private GitUri gitUri;

    /**
     * This will commit the local changes and push them to the repository. If
     * the method is unable to push to the repository without force, it will
     * throw an exception. {@inheritDoc}
     */
    @Override
    public void closeConnection() throws ConnectionException {
        try {
            git.add().addFilepattern(".").call(); //$NON-NLS-1$
            git.commit().setMessage(R.getString("commitmessage")).call(); //$NON-NLS-1$
            git.push().setRemote(gitUri.getGitRepositoryUri())
                    .setCredentialsProvider(credentialsProvider).call();
        } catch (final GitAPIException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    /**
     * This will read from the working copy. File modification date would not be
     * available as it does not really have any meaningful value. {@inheritDoc}
     * 
     * @throws ResourceDoesNotExistException
     *             when the file does not exist
     * @throws AuthorizationException
     *             when the file cannot be read
     */
    @Override
    public void fillInputData(final InputData inputData)
            throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
        try {
            final File file = new File(gitDir, inputData.getResource()
                    .getName());
            if (!file.exists()) {
                throw new ResourceDoesNotExistException(format(
                        R.getString("filenotfound"), file)); //$NON-NLS-1$
            }
            if (!file.canRead()) {
                throw new AuthorizationException(format(
                        R.getString("cannotreadfile"), file)); //$NON-NLS-1$
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
            if (!file.getParentFile().mkdirs()
                    && !file.getParentFile().exists()) {
                throw new TransferFailedException(format(
                        R.getString("unabletocreatedirs"), //$NON-NLS-1$
                        file.getParentFile()));
            }
            outputData.setOutputStream(new FileOutputStream(file));
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFileList(final String destinationDirectory)
            throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
        final File dir = new File(gitDir, destinationDirectory);
        final File[] files = dir.listFiles();
        if (files == null) {
            throw new ResourceDoesNotExistException(format(
                    R.getString("dirnotfound"), dir)); //$NON-NLS-1$
        }
        final List<String> list = new LinkedList<String>();
        for (final File file : files) {
            String name = file.getName();
            if (file.isDirectory() && !name.endsWith("/")) { //$NON-NLS-1$
                name += "/"; // NOPMD this is easier to read. //$NON-NLS-1$
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
            gitDir = File.createTempFile("wagon-git", null); //$NON-NLS-1$
            gitDir.delete();
            gitDir.mkdir();

            credentialsProvider = new UsernamePasswordCredentialsProvider(
                    getAuthenticationInfo().getUserName(),
                    getAuthenticationInfo().getPassword() == null ? "" //$NON-NLS-1$
                            : getAuthenticationInfo().getPassword());
            git = Git.cloneRepository().setURI(gitUri.getGitRepositoryUri())
                    .setCredentialsProvider(credentialsProvider)
                    .setBranch(gitUri.getBranchName()).setDirectory(gitDir)
                    .call();
            if (!gitUri.getBranchName().equals(git.getRepository().getBranch())) {
                final RefUpdate refUpdate = git.getRepository()
                        .getRefDatabase().newUpdate(Constants.HEAD, true);
                refUpdate.setForceUpdate(true);
                refUpdate.link("refs/heads/" + gitUri.getBranchName()); //$NON-NLS-1$
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
     * {@inheritDoc}
     */
    @Override
    public void putDirectory(final File sourceDirectory,
            final String destinationDirectory) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        try {
            if (!sourceDirectory.isDirectory()) {
                throw new ResourceDoesNotExistException(format(
                        R.getString("dirnotfound"), sourceDirectory)); //$NON-NLS-1$
            }
            FileUtils.copyDirectoryStructure(sourceDirectory, new File(gitDir,
                    destinationDirectory));
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc} Does not throw anything.
     */
    @Override
    public boolean resourceExists(final String resourceName) {
        final File file = new File(gitDir, resourceName);

        if (resourceName.endsWith("/")) { //$NON-NLS-1$
            return file.isDirectory();
        }

        return file.exists();
    }

    /**
     * Directory copy is supported.
     * 
     * @return true
     */
    @Override
    public boolean supportsDirectoryCopy() {
        return true;
    }
}
