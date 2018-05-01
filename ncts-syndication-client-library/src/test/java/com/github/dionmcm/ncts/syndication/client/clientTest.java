package com.github.dionmcm.ncts.syndication.client;


import org.testng.annotations.Test;

import com.google.common.net.MediaType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.jdom2.JDOMException;
import org.junit.Rule;
import org.mockserver.client.*;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.ConnectionOptions;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;



public class clientTest {
	
	SyndicationClient testClient;
	String feedURL = "http://localhost:1080/syndication.xml";
	String tokenURL = "http://localhost:1080/mockToken";
	String clientID = "test";
	String secret = "test";
	File outDir = new File("src/test/java/com/github/dionmcm/ncts/syndication/client/Test_Client");

	private ClientAndServer mockServer;
	private MockServerClient mockServerClient;
	String [] serverFileList = { //That are contained in the "Test_Server" folder
			"blue1.r2",
			"blue2.r2",
			"red1.r2",
			"purple1.r2",
			"purple2.r2",
			"green1.r2"
	};
	String serverDir = "src/test/java/com/github/dionmcm/ncts/syndication/client/Test_Server/";

	
    @Test(priority = 1, groups = "downloading", enabled = true)
    public void downloadsAllFilesInCategory() throws URISyntaxException, IOException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException {
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
    	Map<String, List<DownloadResult>> result = testClient.download(false, "SCT_RF2_BLUE");
    	
    	List<DownloadResult> blueResults = result.get("SCT_RF2_BLUE");
    	
    	//return list of all downloaded blue files
    	List<String> downloadedFiles = blueResults.stream()
    									.map(file -> file.getFile().getName())
    									.collect(Collectors.toList());
    	
    	assertTrue(downloadedFiles.size() == 2);
    	
    	//assert that all blue results in list
    	assertTrue(
    		downloadedFiles.contains("blue1.r2") &&
    		downloadedFiles.contains("blue2.r2")
    	);
    	
    	//assert files not missing from local directory
    	List<String> filesInClientFolder = Files.list(outDir.toPath())
    										.map(file->file.getFileName().toString())
    										.collect(Collectors.toList());
    	assertTrue(
    			filesInClientFolder.contains("blue1.r2") &&
    			filesInClientFolder.contains("blue2.r2")
    	);
    }

    @Test(priority = 2, groups = "downloading", enabled = true )
    public void downloadsLatestInCategory() throws IOException, URISyntaxException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException {
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
    	DownloadResult result = testClient.downloadLatest("SCT_RF2_PURPLE");
    	assertTrue(result.getFile().getName().equals("purple2.r2"));
    	
    	List<String> filesInClientFolder = Files.list(outDir.toPath())
				.map(file->file.getFileName().toString())
				.collect(Collectors.toList());
		assertTrue(
				filesInClientFolder.contains("purple2.r2")
		);
    }
    
    @Test(priority = 3, groups = "downloading", enabled = true, expectedExceptions = HashValidationFailureException.class)
    public void hashMismatchInSyndicationThrowsException() throws IOException, URISyntaxException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException{
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
    	DownloadResult result = testClient.downloadLatest("SCT_RF2_GREEN");
    	//Throw HashValidationFailureException
    }
    
    @Test(priority = 4, groups = "downloading", enabled = true )
    public void downloadsFilesFromMultipleCategories() throws IOException, URISyntaxException, NoSuchAlgorithmException, JDOMException, HashValidationFailureException {
    	testClient = new SyndicationClient(feedURL,tokenURL, outDir, clientID, secret);
    	Map<String, List<DownloadResult>> result = testClient.download(false, "SCT_RF2_PURPLE", "SCT_RF2_RED", "SCT_RF2_BLUE");
    	
    	//collapse download results into 1 dimensional list
    	List<String> downloadedFiles = result.values().stream().flatMap(category -> category.stream())
    			.map(file -> file.getFile().getName())
    			.collect(Collectors.toList());
    	
    	assertTrue(downloadedFiles.size() == 5);
    	    	
    	assertTrue(
    			downloadedFiles.contains("purple1.r2") &&
    			downloadedFiles.contains("purple2.r2") &&
    			downloadedFiles.contains("red1.r2") &&
    			downloadedFiles.contains("blue1.r2") &&
    			downloadedFiles.contains("blue2.r2") 
        );
    	
    	//assert files not missing from local directory
    	List<String> filesInClientFolder = Files.list(outDir.toPath())
    										.map(file->file.getFileName().toString())
    										.collect(Collectors.toList());
    	assertTrue(
    			filesInClientFolder.contains("purple1.r2") &&
    			filesInClientFolder.contains("purple2.r2") &&
    			filesInClientFolder.contains("red1.r2") &&
    			filesInClientFolder.contains("blue1.r2") &&
    			filesInClientFolder.contains("blue2.r2")
    	);
    }

    
    @BeforeClass
    public void setUpMockServer() throws IOException
    {
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

    public static void main(String[] theArgs) throws Exception {

    }

}

