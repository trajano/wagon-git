package net.trajano.wagon.git.internal;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.UserInfo;

public class AcceptAllHostKeyRepository implements
    HostKeyRepository {

    /**
     * {@inheritDoc}
     */
    @Override
    public int check(String host,
        byte[] key) {

        return HostKeyRepository.OK;
    }

    /**
     * Does nothing. {@inheritDoc}
     */
    @Override
    public void add(HostKey hostkey,
        UserInfo ui) {

    }

    /**
     * Does nothing. {@inheritDoc}
     */
    @Override
    public void remove(String host,
        String type) {

    }

    /**
     * Does nothing. {@inheritDoc}
     */
    @Override
    public void remove(String host,
        String type,
        byte[] key) {

    }

    /**
     * {@inheritDoc}
     * 
     * @return the class name
     */
    @Override
    public String getKnownHostsRepositoryID() {

        return this.getClass().getName();
    }

    @Override
    public HostKey[] getHostKey() {

        return new HostKey[0];
    }

    @Override
    public HostKey[] getHostKey(String host,
        String type) {

        return new HostKey[0];
    }

}
