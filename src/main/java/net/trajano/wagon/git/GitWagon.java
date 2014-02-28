package net.trajano.wagon.git;

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
 * Module object.
 */
@Component(role = Wagon.class, hint = "git", instantiationStrategy = "per-lookup")
public class GitWagon extends StreamWagon {

    @Override
    public void fillInputData(InputData inputData)
            throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
        // TODO Auto-generated method stub

    }

    @Override
    public void fillOutputData(OutputData outputData)
            throws TransferFailedException {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeConnection() throws ConnectionException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void openConnectionInternal() throws ConnectionException,
            AuthenticationException {
        // TODO Auto-generated method stub

    }
}
