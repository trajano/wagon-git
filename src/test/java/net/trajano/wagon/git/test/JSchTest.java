package net.trajano.wagon.git.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.junit.Test;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import net.trajano.wagon.git.internal.AgentJschConfigSessionFactory;

public class JSchTest {

    /**
     * Tests connection with config session factory
     */
    @Test
    public void testConnectWithConfigSessionFactory() throws Exception {

        final AuthenticationInfo i = new AuthenticationInfo();
        i.setUserName("git");
        try {
            i.setPrivateKey(new File(Thread.currentThread().getContextClassLoader().getResource("github").toURI())
                .getAbsolutePath());
        } catch (URISyntaxException e) {
            throw new AssertionError(e.getMessage());
        }
        i.setPassphrase(System.getenv("SONAR_GITHUB_TOKEN"));

        final JSch jsch = new AgentJschConfigSessionFactory(i).createDefaultJSch(null);
        assertNotNull(jsch);
        final Session session = jsch.getSession("git", "github.com");
        session.connect(2000);
        assertTrue(session.isConnected());
        session.disconnect();

    }

}
