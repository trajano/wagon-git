package net.trajano.wagon.git.test;

import static org.junit.Assert.assertEquals;
import net.trajano.wagon.git.GitUri;

import org.junit.Test;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Type;

public class DnsLookupTest {
    @Test
    public void testGithubPages() throws Exception {
        final GitUri uri = GitUri
                .buildFromGithubPages("githubpages:http://site.trajano.net/foo");
        assertEquals("ssh://git@github.com/trajano/foo.git",
                uri.getGitRepositoryUri());
    }

    @Test
    public void testGithubPagesWithCname() throws Exception {
        final GitUri uri = GitUri
                .buildFromGithubPages("githubpages:http://twitter.github.io/bootstrap");
        assertEquals("ssh://git@github.com/twitter/bootstrap.git",
                uri.getGitRepositoryUri());
    }

    @Test
    public void testSiteTrajanoNet() throws Exception {
        final Lookup lookup = new Lookup("site.trajano.net", Type.CNAME);
        lookup.run();
        assertEquals("trajano.github.io.", ((CNAMERecord) lookup.run()[0])
                .getTarget().toString());
    }
}
