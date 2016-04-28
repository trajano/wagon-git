package net.trajano.wagon.git.internal;

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

    /**
     * Constructs the provider using a given passphrase.
     *
     * @param passphrase
     *            passphrase
     */
    public PassphraseCredentialsProvider(final String passphrase) {
        super();
        this.passphrase = passphrase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean get(final URIish uriish,
            final CredentialItem... items) {

        for (final CredentialItem item : items) {
            if (item instanceof CredentialItem.StringType) {
                ((CredentialItem.StringType) item).setValue(passphrase);
            }
            // This will automatically say "yes" to prompts
            // TODO make this explicit to just accept host
            // TODO make this configurable
            if (item instanceof CredentialItem.YesNoType) {
                ((CredentialItem.YesNoType) item).setValue(true);
            }
        }
        return true;
    }

    /**
     * This provider does not interact with the user, it pulls directly from the
     * value that was set in the constructor.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean isInteractive() {

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return <code>true</code> when items contains a {@link CredentialItem}
     *         .StringType or .YesNoType
     */
    @Override
    public boolean supports(final CredentialItem... items) {

        for (final CredentialItem item : items) {
            if (item instanceof CredentialItem.StringType) {
                return true;
            }
            if (item instanceof CredentialItem.YesNoType) {
                return true;
            }
        }
        return false;
    }
}
