package net.trajano.wagon.git.test;

import java.io.File;
import java.io.IOException;

import org.apache.maven.wagon.StreamingWagonTestCase;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

public class GitWagonTest extends StreamingWagonTestCase {

    @Override
    protected String getTestRepositoryUrl() throws IOException {
        return "git:ssh://git@github.com/trajano/trajano.git#gh-pages";
    }

    @Override
    protected String getProtocol() {
        return "git";
    }

    @Override
    protected int getTestRepositoryPort() {
        return 0;
    }

    @Override
    protected long getExpectedLastModifiedOnGet(Repository repository,
            Resource resource) {
        return new File(new File("target/git"), resource.getName())
                .lastModified();
    }
}
