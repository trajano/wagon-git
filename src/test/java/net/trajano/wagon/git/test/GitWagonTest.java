package net.trajano.wagon.git.test;

import java.io.IOException;

import org.apache.maven.wagon.StreamingWagonTestCase;

public class GitWagonTest extends StreamingWagonTestCase {

    @Override
    protected String getTestRepositoryUrl() throws IOException {
        return "git:ssh://git@github.com/trajano/trajano.git#gh-pages";
    }

    @Override
    protected String getProtocol() {
        return null;
    }

    @Override
    protected int getTestRepositoryPort() {
        return 0;
    }

}
