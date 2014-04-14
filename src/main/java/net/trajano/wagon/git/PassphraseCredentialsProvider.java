package net.trajano.wagon.git;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * A {@link CredentialsProvider} that takes in a passphrase.
 */
public class PassphraseCredentialsProvider extends CredentialsProvider {
    /**
     * Passphrase.
     */
    private final String passphrase;

    public PassphraseCredentialsProvider(final String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public boolean get(final URIish uriish, final CredentialItem... items)
            throws UnsupportedCredentialItem {
        for (final CredentialItem item : items) {
            if (item instanceof CredentialItem.StringType) {
                ((CredentialItem.StringType) item).setValue(passphrase);
                continue;
            }
        }
        return true;
    }

    @Override
    public boolean isInteractive() {
        return true;
    }

    @Override
    public boolean supports(final CredentialItem... items) {
        return true;
    }
}
