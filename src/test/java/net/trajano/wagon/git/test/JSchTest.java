package net.trajano.wagon.git.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import net.trajano.wagon.git.GitHubPagesWagon;
import net.trajano.wagon.git.internal.AgentJschConfigSessionFactory;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.junit.Test;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Type;

public class JSchTest {

    /**
     * Tests connection with config session factory
     */
    @Test
    public void testConnectWithConfigSessionFactory() throws Exception {

        final AuthenticationInfo i = new AuthenticationInfo();
        i.setUserName("git");
        i.setPrivateKey(System.getenv("HOME") + "/.m2/github");
        i.setPassphrase(System.getenv("SONAR_GITHUB_TOKEN"));

        final JSch jsch = new AgentJschConfigSessionFactory(i).createDefaultJSch(null);
        assertNotNull(jsch);
        final Session session = jsch.getSession("git", "github.com");
        session.connect(2000);
        assertTrue(session.isConnected());
        session.disconnect();

    }

}
