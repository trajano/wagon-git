package net.trajano.wagon.git.internal;

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
     * Does nothing. {@inheritDoc}
     */
    @Override
    protected void configure(final Host hc,
            final Session session) {

    }

    /**
     * Tries to connect to a SSH agent, if it fails, it uses the default.
     */
    @Override
    protected JSch createDefaultJSch(final FS fs) throws JSchException {

        try {
            final USocketFactory usf = new JNAUSocketFactory();
            final Connector con = new SSHAgentConnector(usf);
            final JSch jsch = new JSch();
            JSch.setConfig("PreferredAuthentications", "publickey");
            final IdentityRepository irepo = new RemoteIdentityRepository(con);
            jsch.setIdentityRepository(irepo);
            return jsch;
	} catch (final UnsatisfiedLinkError e) {
            return super.createDefaultJSch(fs);
        } catch (final AgentProxyException e) {
            return super.createDefaultJSch(fs);
        }
    }
}
