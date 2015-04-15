package net.trajano.wagon.git.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.trajano.wagon.git.internal.PassphraseCredentialsProvider;

import org.eclipse.jgit.transport.CredentialItem;
import org.junit.Test;

public class PassphraseCredentialsProviderTest {

    @Test
    public void testCredentials() throws Exception {

        final CredentialItem.StringType i = new CredentialItem.StringType("enter passphrase", true);
        final CredentialItem i2 = new CredentialItem.InformationalMessage("info");
        final PassphraseCredentialsProvider provider = new PassphraseCredentialsProvider("passphrase");
        assertTrue(provider.get(null, i, i2));
        assertEquals("passphrase", i.getValue());
        assertFalse(provider.isInteractive());
        assertTrue(provider.supports(i, i2));
        assertTrue(provider.supports(i));
        assertFalse(provider.supports(i2));
        assertFalse(provider.supports());
    }

    @Test
    public void testEmptyCredentials() throws Exception {

        final PassphraseCredentialsProvider provider = new PassphraseCredentialsProvider("passphrase");
        assertTrue(provider.get(null));
        assertFalse(provider.supports());
    }
}
