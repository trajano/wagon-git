package net.trajano.wagon.git.internal;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

/**
 * Enables ssh-agent support using JSCH.
 *
 * @author Archimedes Trajano
 */
public class JSchAgentCapableTransportConfigCallback implements
    TransportConfigCallback {

    /**
     * Authentication info.
     */
    private final AuthenticationInfo authenticationInfo;

    /**
     * Constructs the callback.
     * 
     * @param authenticationInfo
     *            authentication info
     */
    public JSchAgentCapableTransportConfigCallback(final AuthenticationInfo authenticationInfo) {

        this.authenticationInfo = authenticationInfo;
    }

    /**
     * Configures the transport to work with ssh-agent if an SSH transport is
     * being used. {@inheritDoc}
     */
    @Override
    public void configure(final Transport transport) {

        if (transport instanceof SshTransport) {
            final SshTransport sshTransport = (SshTransport) transport;
            final SshSessionFactory sshSessionFactory = new AgentJschConfigSessionFactory(authenticationInfo);
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }
    }
}
