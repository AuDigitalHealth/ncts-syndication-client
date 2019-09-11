package au.gov.digitalhealth.ncts.syndication.client;

import static org.easymock.EasyMock.expect;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Collections;
import org.easymock.EasyMockSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import au.gov.digitalhealth.ncts.syndication.client.exception.HashValidationFailureException;

@Test(singleThreaded = true)
public class DownloadSyndicationArtefactTest extends EasyMockSupport {

    private static final String TEST_SECRET = "clientSecret";
    private static final String TEST_OUT_PATH = "/tmp";
    private static final String TEST_CLIENT_ID = "clientid";
    private static final String TEST_TOKEN_URL = "http://token.url";
    private static final String TEST_FEED_URL = "http://feed.url";

    @BeforeMethod
    public void setUp() {
        DownloadSyndicationArtefact.client = mock(SyndicationClient.class);
        resetAll();
    }

    @Test(description = "category parameter only")
    public void categoryTestOnly() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        expect(DownloadSyndicationArtefact.client.setFeedUrl(SyndicationClient.FEED_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setTokenUrl(SyndicationClient.TOKEN_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setOutputDirectory(new File(System.getProperty("user.dir"))))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientId(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientSecret(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.download(false, "foo")).andReturn(null);
        replayAll();

        DownloadSyndicationArtefact.main(new String[] { "-category", "foo" });

        verifyAll();
    }

    @Test(description = "category parameter and latest")
    public void categoryTestAndLatest() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        expect(DownloadSyndicationArtefact.client.setFeedUrl(SyndicationClient.FEED_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setTokenUrl(SyndicationClient.TOKEN_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setOutputDirectory(new File(System.getProperty("user.dir"))))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientId(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientSecret(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.download(true, "foo")).andReturn(null);
        replayAll();

        DownloadSyndicationArtefact.main(new String[] { "-latest", "-category", "foo" });

        verifyAll();
    }

    @Test(description = "category and contentItemId parameters")
    public void categoryTestAndContentItemId() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        expect(DownloadSyndicationArtefact.client.setFeedUrl(SyndicationClient.FEED_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setTokenUrl(SyndicationClient.TOKEN_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setOutputDirectory(new File(System.getProperty("user.dir"))))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientId(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientSecret(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.downloadByCategoryAndContentItemId(
            Collections.singletonList("foo"), Arrays.asList("someId", "someOtherId"), false)).andReturn(null);
        replayAll();

        DownloadSyndicationArtefact.main(new String[] { "-category", "foo", "-contentItemId",
            "someId", "-contentItemId", "someOtherId" });

        verifyAll();
    }

    @Test(description = "category and contentItemId parameters, latest only")
    public void categoryTestContentItemIdAndLatest() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        expect(DownloadSyndicationArtefact.client.setFeedUrl(SyndicationClient.FEED_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setTokenUrl(SyndicationClient.TOKEN_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setOutputDirectory(new File(System.getProperty("user.dir"))))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientId(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientSecret(null)).andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.downloadByCategoryAndContentItemId(
            Collections.singletonList("foo"), Arrays.asList("someId", "someOtherId"), true)).andReturn(null);
        replayAll();

        DownloadSyndicationArtefact.main(new String[] { "-latest", "-category", "foo",
            "-contentItemId", "someId", "-contentItemId", "someOtherId" });

        verifyAll();
    }

    @Test(description = "all parameters set")
    public void allParameters() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        expect(DownloadSyndicationArtefact.client.setFeedUrl(TEST_FEED_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setTokenUrl(TEST_TOKEN_URL))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setOutputDirectory(new File(TEST_OUT_PATH)))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientId(TEST_CLIENT_ID))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.setClientSecret(TEST_SECRET))
            .andReturn(DownloadSyndicationArtefact.client);
        expect(DownloadSyndicationArtefact.client.download(true, "foo")).andReturn(null);
        replayAll();

        DownloadSyndicationArtefact.main(new String[] { "-category", "foo", "-feed", TEST_FEED_URL, "-token",
                TEST_TOKEN_URL, "-id", TEST_CLIENT_ID, "-latest", "-out", TEST_OUT_PATH, "-secret", TEST_SECRET });

        verifyAll();
    }

    @Test(description = "request help message")
    public void printHelp() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        DownloadSyndicationArtefact.main(new String[] { "-category", "foo", "-?" });
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.contains("-?,--help                       Prints the parameter usage help"),
            allWrittenLines);
    }

    @Test(description = "missing category", expectedExceptions = { IllegalArgumentException.class })
    public void missingCategory() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        DownloadSyndicationArtefact.main(new String[] { "-latest" });
    }

    @Test(description = "invalid arguments", expectedExceptions = { IllegalArgumentException.class })
    public void invalidArguments() throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        DownloadSyndicationArtefact.main(new String[] { "-foo", "foo", "-?" });
    }
}
