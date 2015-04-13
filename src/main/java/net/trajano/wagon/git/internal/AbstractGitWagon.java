package net.trajano.wagon.git.internal;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Common parts of handling Git based repositories.
 */
public abstract class AbstractGitWagon extends StreamWagon {

    /**
     * Logger.
     */
    private static final Logger LOG;

    /**
     * Messages resource path.
     */
    private static final String MESSAGES = "META-INF/Messages";

    /**
     * Resource bundle.
     */
    private static final ResourceBundle R;

    static {
        LOG = Logger.getLogger("net.trajano.wagon.git", MESSAGES);
        R = ResourceBundle.getBundle(MESSAGES);
    }

    /**
     * Credentials provider.
     */
    private CredentialsProvider credentialsProvider;

    /**
     * Git cache.
     */
    private final ConcurrentMap<String, Git> gitCache = new ConcurrentHashMap<String, Git>();

    /**
     * Git URI.
     */
    private GitUri gitUri;

    /**
     * Builds the wagon specific Git URI based on the repository URL. This is
     * made public rather than protected to allow testing of the method.
     *
     * @param repositoryUrl
     *            repository URL
     * @return Git URI
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract GitUri buildGitUri(URI repositoryUrl) throws IOException,
    URISyntaxException;

    /**
     * This will commit the local changes and push them to the repository. If
     * the method is unable to push to the repository without force, it will
     * throw an exception. {@inheritDoc}
     */
    @Override
    public void closeConnection() throws ConnectionException {

        try {
            for (final Entry<String, Git> gitEntry : gitCache.entrySet()) {
                final Git git = gitEntry.getValue();
                git.add()
                .addFilepattern(".").call(); //$NON-NLS-1$
                git.commit()
                .setMessage(R.getString("commitmessage")).call(); //$NON-NLS-1$
                git.push()
                .setRemote(gitEntry.getKey())
                .setCredentialsProvider(credentialsProvider)
                .call();
                git.close();
                FileUtils.deleteDirectory(git.getRepository()
                        .getDirectory());
            }
        } catch (final GitAPIException e) {
            throw new ConnectionException(e.getMessage(), e);
        } catch (final IOException e) {
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
    public void fillInputData(final InputData inputData) throws TransferFailedException,
    ResourceDoesNotExistException,
    AuthorizationException {

        try {
            final File file = getFileForResource(inputData.getResource()
                    .getName());
            if (!file.exists()) {
                throw new ResourceDoesNotExistException(format(R.getString("filenotfound"), file)); //$NON-NLS-1$
            }
            if (!file.canRead()) {
                throw new AuthorizationException(format(R.getString("cannotreadfile"), file)); //$NON-NLS-1$
            }
            inputData.setInputStream(new FileInputStream(file));
            inputData.getResource()
            .setContentLength(file.length());
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final GitAPIException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    /**
     * This will write to the working copy. {@inheritDoc}
     */
    @Override
    public void fillOutputData(final OutputData outputData) throws TransferFailedException {

        try {
            final File file = getFileForResource(outputData.getResource()
                    .getName());
            if (!file.getParentFile()
                    .mkdirs() && !file.getParentFile()
                    .exists()) {
                throw new TransferFailedException(format(R.getString("unabletocreatedirs"), //$NON-NLS-1$
                        file.getParentFile()));
            }
            outputData.setOutputStream(new FileOutputStream(file));
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final GitAPIException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    /**
     * This will get the file object for the given resource relative to the
     * {@link Git} specified for the connection. It will handle resources where
     * it jumps up past the parent folder.
     *
     * @param resourceName
     *            resource name.
     * @return file used for the resource.
     * @throws IOException
     * @throws GitAPIException
     *             problem with the GIT API.
     * @throws URISyntaxException
     * @throws ResourceDoesNotExistException
     */
    protected abstract File getFileForResource(String resourceName) throws GitAPIException,
    IOException,
    URISyntaxException;

    /**
     * {@inheritDoc}
     * <p>
     * Warnings are suppressed for false positive with Sonar and multiple
     * exceptions on public API. {@inheritDoc}
     * </p>
     */
    @Override
    @SuppressWarnings("all")
    public List<String> getFileList(final String directory) throws TransferFailedException,
    ResourceDoesNotExistException,
    AuthorizationException {

        final File dir;
        try {
            dir = getFileForResource(directory);
        } catch (final GitAPIException e) {
            throw new AuthorizationException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new ResourceDoesNotExistException(e.getMessage(), e);
        }
        final File[] files = dir.listFiles();
        if (files == null) {
            throw new ResourceDoesNotExistException(format(R.getString("dirnotfound"), dir)); //$NON-NLS-1$
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
     * be pulled cleanly this method will fail.
     *
     * @param gitRepositoryUri
     *            remote git repository URI string
     * @return git
     * @throws GitAPIException
     * @throws IOException
     * @throws URISyntaxException
     * @thorws ResourceDoesNotExistException remote repository does not exist.
     */
    protected Git getGit(final String gitRepositoryUri) throws GitAPIException,
    IOException,
    URISyntaxException,
    ResourceDoesNotExistException {

        final Git cachedGit = gitCache.get(gitRepositoryUri);
        if (cachedGit != null) {
            return cachedGit;
        }
        final File gitDir = File.createTempFile(gitRepositoryUri.replaceAll("[^A-Za-z]", "_"), "wagon-git"); //$NON-NLS-1$
        gitDir.delete();
        gitDir.mkdir();

        if (getAuthenticationInfo().getUserName() != null) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(getAuthenticationInfo().getUserName(), getAuthenticationInfo().getPassword() == null ? "" //$NON-NLS-1$
                    : getAuthenticationInfo().getPassword());
        } else {
            credentialsProvider = new PassphraseCredentialsProvider(getAuthenticationInfo().getPassword());
        }
        try {
            final Git git = Git.cloneRepository()
                    .setURI(gitRepositoryUri)
                    .setCredentialsProvider(credentialsProvider)
                    .setBranch(gitUri.getBranchName())
                    .setDirectory(gitDir)
                    .call();
            if (!gitUri.getBranchName()
                    .equals(git.getRepository()
                            .getBranch())) {
                LOG.log(Level.INFO, "missingbranch", gitUri.getBranchName());
                final RefUpdate refUpdate = git.getRepository()
                        .getRefDatabase()
                        .newUpdate(Constants.HEAD, true);
                refUpdate.setForceUpdate(true);
                refUpdate.link("refs/heads/" + gitUri.getBranchName()); //$NON-NLS-1$
            }
            gitCache.put(gitRepositoryUri, git);
            return git;
        } catch (final InvalidRemoteException e) {
            throw new ResourceDoesNotExistException(e.getMessage(), e);
        } catch (final NoRemoteRepositoryException e) {
            throw new ResourceDoesNotExistException(e.getMessage(), e);
        }
    }

    protected GitUri getGitUri() {

        return gitUri;
    }

    /**
     * Sets the initial git URI.
     */
    @Override
    protected void openConnectionInternal() throws ConnectionException,
    AuthenticationException {

        URI uri;
        try {
            uri = new URI(new URI(getRepository().getUrl()
                    .replace("##", "#")).getSchemeSpecificPart()).normalize();
            gitUri = buildGitUri(uri);
        } catch (final URISyntaxException e) {
            throw new ConnectionException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    /**
     * If the destination directory is not inside the source directory (denoted
     * by starting with "../"), then another git repository is registered.
     * Warnings are suppressed for false positive with Sonar and multiple
     * exceptions on public API. {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public void putDirectory(final File sourceDirectory,
            final String destinationDirectory) throws TransferFailedException,
            ResourceDoesNotExistException,
            AuthorizationException {

        try {
            if (!sourceDirectory.isDirectory()) {
                throw new ResourceDoesNotExistException(format(R.getString("dirnotfound"), sourceDirectory)); //$NON-NLS-1$
            }
            final File fileForResource = getFileForResource(destinationDirectory);
            if (fileForResource == null) {
                throw new ResourceDoesNotExistException(format(R.getString("dirnotfound"), destinationDirectory)); //$NON-NLS-1$
            }
            FileUtils.copyDirectoryStructure(sourceDirectory, fileForResource);
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final GitAPIException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resourceExists(final String resourceName) throws TransferFailedException {

        final File file;
        try {
            file = getFileForResource(resourceName);
        } catch (final GitAPIException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        } catch (final URISyntaxException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }

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
