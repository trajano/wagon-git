package net.trajano.wagon.git.internal;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

/**
 * A {@link JschConfigSessionFactory} that tries to use an ssh-agent.
 *
 * @author Archimedes
 */
public final class AgentJschConfigSessionFactory extends JschConfigSessionFactory {

    /**
     * Authentication info.
     */
    private final AuthenticationInfo authenticationInfo;

    public AgentJschConfigSessionFactory(final AuthenticationInfo authenticationInfo) {

        this.authenticationInfo = authenticationInfo;
    }

    /**
     * Does nothing. {@inheritDoc}
     */
    @Override
    protected void configure(final Host hc,
            final Session session) {

    }

    /**
     * Tries using the custom private key first, if not then it tries to connect to a SSH agent, if it fails, it uses the default.
     */
    @Override
    public JSch createDefaultJSch(final FS fs) throws JSchException {

        try {
            final JSch jsch = new JSch();
            JSch.setConfig("PreferredAuthentications", "publickey");
            if (authenticationInfo.getPrivateKey() != null) {
                if (authenticationInfo.getPassphrase() != null) {
                    jsch.addIdentity(authenticationInfo.getPrivateKey(), authenticationInfo.getPassphrase());
                } else {
                    jsch.addIdentity(authenticationInfo.getPrivateKey());
                }
            } else {
                final USocketFactory usf = new JNAUSocketFactory();
                final Connector con = new SSHAgentConnector(usf);
                final IdentityRepository irepo = new RemoteIdentityRepository(con);
                jsch.setIdentityRepository(irepo);
            }
            return jsch;
	} catch (final UnsatisfiedLinkError e) {
            return super.createDefaultJSch(fs);
        } catch (final AgentProxyException e) {
            return super.createDefaultJSch(fs);
        }
    }
}
