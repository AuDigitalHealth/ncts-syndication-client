package au.gov.digitalhealth.ncts.syndication.client;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import org.jdom2.JDOMException;
import org.testng.annotations.Test;

import au.gov.digitalhealth.ncts.syndication.client.exception.HashValidationFailureException;

public class CommandLineTest {
  @Test
  public void f() throws NoSuchAlgorithmException, URISyntaxException, JDOMException, IOException, HashValidationFailureException {
	  String [] options = {
			  "-secret", "mysecret",
			  "-id", "myid",
			  "-token", "http://localhost/token",
			  "-feed", "http://localhost/feed.xml",
			  "-out", "output.csv",
			  "-category", "BLUE",
			  "-latest", "false"
	  };
	  try {
		  DownloadSyndicationArtefact.main(options);
	  }catch(Exception e) {
		 
	  }
  }
}
