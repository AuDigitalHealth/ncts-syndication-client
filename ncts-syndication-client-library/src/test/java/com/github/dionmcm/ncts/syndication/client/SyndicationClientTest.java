package com.github.dionmcm.ncts.syndication.client;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jdom2.JDOMException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SyndicationClientTest {
	
    private static final String SCT_RF2_RED_CATEGORY = "SCT_RF2_RED";
    private static final String SCT_RF2_PURPLE_CATEGORY = "SCT_RF2_PURPLE";
    private static final String SCT_RF2_BLUE_CATEGORY = "SCT_RF2_BLUE";
    private static final String feedURL = "http://localhost:1080/syndication.xml";
    private static final String tokenURL = "http://localhost:1080/mockToken";
    private static final String clientID = "test";
    private static final String secret = "test";
    private static final String serverDir = "target/test-classes/"; // where the server resources are
    private static final File outDir = new File("target/client-output"); // where the client under test will download to
    private static final String[] serverFileList = { // That are contained in the test resources folder
            "blue1.r2",
            "blue2.r2",
            "red1.r2",
            "purple1.r2",
            "purple2.r2",
            "green1.r2"
    };

    private SyndicationClient testClient;
	private ClientAndServer mockServer;
	private MockServerClient mockServerClient;
	
    @Test(priority = 1, groups = "downloading", description = "Tests that the client accurately downloads all files in a single category", enabled = true)
    public void downloadsAllFilesInCategory() throws URISyntaxException, IOException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException {
        testClient = new SyndicationClient(feedURL, tokenURL, outDir, clientID, secret);
        Map<String, List<DownloadResult>> result = testClient.download(false, SCT_RF2_BLUE_CATEGORY);

        List<DownloadResult> blueResults = result.get(SCT_RF2_BLUE_CATEGORY);

        List<String> downloadedFiles = getDownloadedFileNames(blueResults);

        assertEquals(downloadedFiles.size(), 2, "2 files should be reported by the client as downloaded");

        // assert that all blue results in list
        assertTrue(downloadedFiles.contains("blue1.r2"),
            "blue1.r2 file should be reported by the client as downloaded");
        assertTrue(downloadedFiles.contains("blue2.r2"),
            "blue2.r2 file should be reported by the client as downloaded");

        // assert files not missing from local directory
        List<String> filesInClientFolder = getFilenamesInDownloadsDirectory();
        assertEquals(filesInClientFolder.size(), 2,
            "exactly 2 files should be in the download directory for the client");
        assertTrue(filesInClientFolder.contains("blue1.r2"), "blue1.r2 file should be in the download directory");
        assertTrue(filesInClientFolder.contains("blue2.r2"), "blue2.r2 file should be in the download directory");
    }

    @Test(priority = 2, groups = "downloading", enabled = true )
    public void downloadsLatestInCategory() throws IOException, URISyntaxException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException {
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
        DownloadResult result = testClient.downloadLatest(SCT_RF2_PURPLE_CATEGORY);
        assertTrue(result.getFile().getName().equals("purple2.r2"),
            "only purple2.r2 should be reported by the client as downloaded");
    	
        List<String> filesInClientFolder = getFilenamesInDownloadsDirectory();
        assertEquals(filesInClientFolder.size(), 1,
            "exactly 1 files should be in the download directory for the client");
        assertTrue(filesInClientFolder.contains("purple2.r2"), "purple2.r2 file should be in the download directory");
    }
    
    @Test(priority = 3, groups = "downloading", enabled = true, expectedExceptions = HashValidationFailureException.class)
    public void hashMismatchInSyndicationThrowsException() throws IOException, URISyntaxException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException{
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
        testClient.downloadLatest("SCT_RF2_GREEN");
        // expect HashValidationFailureException using TestNG annotation
    }
    
    @Test(priority = 4, groups = "downloading", enabled = true )
    public void downloadsFilesFromMultipleCategories() throws IOException, URISyntaxException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException {
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
        Map<String, List<DownloadResult>> result =
                testClient.download(false, SCT_RF2_PURPLE_CATEGORY, SCT_RF2_RED_CATEGORY, SCT_RF2_BLUE_CATEGORY);

        assertEquals(result.keySet().size(), 3, "response from the client should contain 3 catagories");
        assertTrue(result.keySet().contains(SCT_RF2_PURPLE_CATEGORY),
            "response from the client should contain the purple catagory");
        assertTrue(result.keySet().contains(SCT_RF2_RED_CATEGORY),
            "response from the client should contain the red catagory");
        assertTrue(result.keySet().contains(SCT_RF2_BLUE_CATEGORY),
            "response from the client should contain the blue catagory");
    	
        // test the purple category
        List<String> downloadedFiles = getDownloadedFileNames(result.get(SCT_RF2_PURPLE_CATEGORY));
        assertEquals(downloadedFiles.size(), 2, "2 purple files should be reported by the client as downloaded");
        assertTrue(downloadedFiles.contains("purple1.r2"),
            "purple1.r2 file should be reported by the client as downloaded");
        assertTrue(downloadedFiles.contains("purple1.r2"),
            "purple1.r2 file should be reported by the client as downloaded");

        // test the red category
        downloadedFiles = getDownloadedFileNames(result.get(SCT_RF2_RED_CATEGORY));
        assertEquals(downloadedFiles.size(), 1, "1 red files should be reported by the client as downloaded");
        assertTrue(downloadedFiles.contains("red1.r2"),
            "red1.r2 file should be reported by the client as downloaded");

        // test the purple category
        downloadedFiles = getDownloadedFileNames(result.get(SCT_RF2_BLUE_CATEGORY));
        assertEquals(downloadedFiles.size(), 2, "2 blue files should be reported by the client as downloaded");
        assertTrue(downloadedFiles.contains("blue1.r2"),
            "blue1.r2 file should be reported by the client as downloaded");
        assertTrue(downloadedFiles.contains("blue2.r2"),
            "blue2.r2 file should be reported by the client as downloaded");
    	
    	//assert files not missing from local directory
        List<String> filesInClientFolder = getFilenamesInDownloadsDirectory();
        assertEquals(filesInClientFolder.size(), 5,
            "exactly 2 files should be in the download directory for the client");
        assertTrue(filesInClientFolder.contains("purple1.r2"), "purple1.r2 file should be in the download directory");
        assertTrue(filesInClientFolder.contains("purple2.r2"), "purple2.r2 file should be in the download directory");
        assertTrue(filesInClientFolder.contains("red1.r2"), "red1.r2 file should be in the download directory");
        assertTrue(filesInClientFolder.contains("blue1.r2"), "blue1.r2 file should be in the download directory");
        assertTrue(filesInClientFolder.contains("blue2.r2"), "blue2.r2 file should be in the download directory");
    }

    
    @BeforeClass
    public void setUpMockServer() throws IOException
    {
        outDir.mkdirs();
    	//Begin mock server and request handler
    	mockServer = startClientAndServer(1080);
    	mockServerClient = new MockServerClient("localhost", 1080);

    	//Put provided server files in memory
    	HashMap<String, byte[]> serverFileBins = new HashMap<String, byte[]>();
    	for(String aFile : serverFileList){
    		Path filePath = new File(serverDir + aFile).toPath();
    		serverFileBins.put(aFile, Files.readAllBytes(filePath));
    	}
    	
    	File syndFilePath = new File(serverDir + "syndication.xml");
        byte[] syndFile = Files.readAllBytes(syndFilePath.toPath());
    	
    	//Set up handling for individual files
    	for(String aFile : serverFileList) {
        	mockServerClient.when(HttpRequest.request().withPath("/" + aFile))
			.respond(
					HttpResponse.response()
					.withBody(serverFileBins.get(aFile))
					);
    	}
    
    	//Handle request for syndication file
    	mockServerClient.when(HttpRequest.request().withPath("/syndication.xml"))
    					.respond(
    							HttpResponse.response()
    							.withBody(syndFile)
    							);
    	    	
    	//Handle request for token (return meaningless Access token, mock server doesn't need token)
    	mockServerClient.when(HttpRequest.request().withPath("/mockToken"))
		.respond(
				HttpResponse.response()
				.withBody("{ \"Access-token\":\"123\"}")
				);
    }
    
    @AfterMethod(alwaysRun=true) @BeforeMethod (alwaysRun=true)
    public void deleteAllFilesInClientFolder() throws IOException {
    	FileUtils.cleanDirectory(outDir); 
    }
    
    @AfterClass
    public void tearDownMockServer() {
        mockServer.stop();
    }

    /**
     * Returns a flat list of file names of the files in the location the NCTS syndication client under test is
     * downloading to
     * 
     * @return List<String> filenames portion of the files in the output directory
     * @throws IOException
     */
    private List<String> getFilenamesInDownloadsDirectory() throws IOException {
        List<String> filesInClientFolder = Files.list(outDir.toPath())
            .map(file -> file.getFileName().toString())
            .collect(Collectors.toList());
        return filesInClientFolder;
    }

    /**
     * Returns a flat list of file names from the dowload results list that the NCTS syndication client responds with
     * 
     * @param downloadResults response List<DownloadedResult> from the NCTS syndication client
     * @return List<String> of just the filename portion of each of the files in the input List<DownloadedResult>
     */
    private List<String> getDownloadedFileNames(List<DownloadResult> downloadResults) {
        return downloadResults.stream()
            .map(file -> file.getFile().getName())
            .collect(Collectors.toList());
    }

}

