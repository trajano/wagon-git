package net.trajano.wagon.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;

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

/**
 * Git Wagon.
 */
@Component(role = Wagon.class, hint = "git", instantiationStrategy = "per-lookup")
public class GitWagon extends StreamWagon {

    private String scmVersion;

    private String scmVersionType;

    /**
     * This will commit the local changes and push them to the repository. If
     * the method is unable to push to the repository without force, it will
     * throw an exception. {@inheritDoc}
     */
    @Override
    public void closeConnection() throws ConnectionException {
        System.out.println("closeConnection");
    }

    /**
     * This will read from the working copy. {@inheritDoc}
     */
    @Override
    public void fillInputData(final InputData inputData)
            throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
        System.out.println("fillInputData" + inputData + " "
                + inputData.getResource().getName());
    }

    /**
     * This will write to the working copy. {@inheritDoc}
     */
    @Override
    public void fillOutputData(final OutputData outputData)
            throws TransferFailedException {
        System.out.println("fillOutputData:" + outputData + " "
                + outputData.getResource().getName());
        outputData.setOutputStream(new ByteArrayOutputStream());
    }

    public String getScmVersion() {
        return scmVersion;
    }

    public String getScmVersionType() {
        return scmVersionType;
    }

    /**
     * This will create or refresh the working copy. If the working copy cannot
     * be pulled cleanly this method will fail. {@inheritDoc}
     */
    @Override
    protected void openConnectionInternal() throws ConnectionException,
            AuthenticationException {
        System.out.println("openConnectionInternal");
        System.out.println(URI.create(getRepository().getUrl())
                .getSchemeSpecificPart());
    }

    @Override
    public void putDirectory(final File sourceDirectory,
            final String destinationDirectory) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        System.out.println("putDirectory " + sourceDirectory + " to "
                + destinationDirectory);
    }

    public void setScmVersion(final String scmVersion) {
        System.out.println("SCMVersion=" + scmVersion);
        this.scmVersion = scmVersion;
    }

    public void setScmVersionType(final String scmVersionType) {
        System.out.println("SCMVersionType=" + scmVersionType);
        this.scmVersionType = scmVersionType;
    }

    @Override
    public boolean supportsDirectoryCopy() {
        return true;
    }
}
