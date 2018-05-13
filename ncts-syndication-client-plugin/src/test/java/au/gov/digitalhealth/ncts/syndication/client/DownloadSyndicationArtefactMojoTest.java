package au.gov.digitalhealth.ncts.syndication.client;

import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.MojoExecutionException;
import org.easymock.EasyMockSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import au.gov.digitalhealth.ncts.syndication.client.exception.HashValidationFailureException;

public class DownloadSyndicationArtefactMojoTest extends EasyMockSupport {
    private static final String TEST_SECRET = "clientSecret";
    private static final String TEST_OUT_PATH = "/tmp";
    private static final String TEST_CLIENT_ID = "clientid";
    private static final String TEST_TOKEN_URL = "http://token.url";
    private static final String TEST_FEED_URL = "http://feed.url";

    DownloadSyndicationArtefactMojo mojo;

    @BeforeMethod
    public void setUp() {
        mojo = new DownloadSyndicationArtefactMojo();
        mojo.client = mock(SyndicationClient.class);
        resetAll();
    }

    @Test(description = "category parameter and latest")
    public void allParameters()
            throws NoSuchAlgorithmException, IOException, HashValidationFailureException, MojoExecutionException {
        expect(mojo.client.setFeedUrl(TEST_FEED_URL)).andReturn(mojo.client);
        expect(mojo.client.setTokenUrl(TEST_TOKEN_URL)).andReturn(mojo.client);
        expect(mojo.client.setOutputDirectory(new File(TEST_OUT_PATH))).andReturn(mojo.client);
        expect(mojo.client.setClientId(TEST_CLIENT_ID)).andReturn(mojo.client);
        expect(mojo.client.setClientSecret(TEST_SECRET)).andReturn(mojo.client);
        expect(mojo.client.download(false, "foo")).andReturn(null);
        replayAll();

        mojo.categories = new String[] { "foo" };
        mojo.feedUrl = TEST_FEED_URL;
        mojo.tokenUrl = TEST_TOKEN_URL;
        mojo.outputDirectory = new File(TEST_OUT_PATH);
        mojo.clientId = TEST_CLIENT_ID;
        mojo.clientSecret = TEST_SECRET;
        mojo.latestOnly = false;

        mojo.execute();

        verifyAll();
    }

    @Test(description = "all parameters set")
    public void allParametersLatest()
            throws NoSuchAlgorithmException, IOException, HashValidationFailureException, MojoExecutionException {
        expect(mojo.client.setFeedUrl(TEST_FEED_URL)).andReturn(mojo.client);
        expect(mojo.client.setTokenUrl(TEST_TOKEN_URL)).andReturn(mojo.client);
        expect(mojo.client.setOutputDirectory(new File(TEST_OUT_PATH))).andReturn(mojo.client);
        expect(mojo.client.setClientId(TEST_CLIENT_ID)).andReturn(mojo.client);
        expect(mojo.client.setClientSecret(TEST_SECRET)).andReturn(mojo.client);
        expect(mojo.client.download(true, "foo")).andReturn(null);
        replayAll();

        mojo.categories = new String[] { "foo" };
        mojo.feedUrl = TEST_FEED_URL;
        mojo.tokenUrl = TEST_TOKEN_URL;
        mojo.outputDirectory = new File(TEST_OUT_PATH);
        mojo.clientId = TEST_CLIENT_ID;
        mojo.clientSecret = TEST_SECRET;
        mojo.latestOnly = true;

        mojo.execute();

        verifyAll();
    }
}
