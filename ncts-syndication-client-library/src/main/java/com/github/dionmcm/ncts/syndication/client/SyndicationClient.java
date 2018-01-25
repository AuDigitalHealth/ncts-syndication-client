package com.github.dionmcm.ncts.syndication.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.JDOMException;

/**
 * Downloads the latest version of a syndication artefact
 */
public class SyndicationClient {
    public static Logger logger = Logger.getLogger(SyndicationClient.class.getName());
    public static final String TOKEN_URL = "https://api.healthterminologies.gov.au/oauth2/token";
    public static final String FEED_URL = "https://api.healthterminologies.gov.au/syndication/v1/syndication.xml";

    URI feedUrl;
    URI tokenUrl;
    File outputDirectory;
    String clientId;
    String clientSecret;

    /**
     * Constructs a new client defaulting the token URL to {@link #TOKEN_URL} and the feed URL to {@link #FEED_URL}.
     * 
     * @param outputDirectory directory to download resources to
     * @param clientId clientID for authentication
     * @param clientSecret client secret for authentication
     * @throws URISyntaxException if the feed or token URLs are invalid URIs
     */
    public SyndicationClient(File outputDirectory, String clientId, String clientSecret) throws URISyntaxException {
        this(FEED_URL, TOKEN_URL, outputDirectory, clientId, clientSecret);
    }

    /**
     * Constructs a new client.
     * 
     * @param feedUrl URL of the syndication feed
     * @param tokenUrl URL to authenticate against
     * @param outputDirectory directory to download resources to
     * @param clientId clientID for authentication
     * @param clientSecret client secret for authentication
     * @throws URISyntaxException if the feed or token URLs are invalid URIs
     */
    public SyndicationClient(String feedUrl, String tokenUrl, File outputDirectory, String clientId,
            String clientSecret) throws URISyntaxException {
        super();
        this.feedUrl = new URI(feedUrl);
        this.tokenUrl = new URI(tokenUrl);
        this.outputDirectory = outputDirectory;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Downloads the specified categories artefacts to the client's download directory specified in the client's
     * constructor. If files are already present in the download directory the SHA256 will be tested, and if the local
     * copy's SHA256 does not match the feed the item will be downloaded over the top of the existing local copy. SHA256
     * hashes are tested for each download and if they fail an exception is thrown and the client aborts.
     * 
     * @param categories syndication feed categories to download, refer to
     *            https://www.healthterminologies.gov.au/specs/v2/conformant-server-apps/syndication-api/syndication-
     *            feed
     * @param latestOnly if true only the latest artefact version from each specified category will be downloaded,
     *            otherwise all artefacts for each category will be downloaded
     * @return a Map containing all the requests categories and a List of {@link DownloadResult}s, one for each aretfact
     *         in the feed matching the categories provided and latestOnly setting
     * @throws JDOMException if the syndication feed cannot be parsed
     * @throws IOException if an error occurs trying to get the feed or its contents
     * @throws NoSuchAlgorithmException if the SHA256 algorithm can't be loaded
     * @throws HashValidationFailureException if the downloaded file's SHA256 doesn't match the hash specified in the
     *             feed
     */
    public Map<String, List<DownloadResult>> download(Set<String> categories, boolean latestOnly)
            throws JDOMException, IOException, NoSuchAlgorithmException, HashValidationFailureException {
        NctsFeedReader feedReader = new NctsFeedReader(feedUrl.toString());
        NctsFileDownloader downloader = new NctsFileDownloader(tokenUrl, clientId, clientSecret);

        Map<String, Set<Entry>> matchingEntries = feedReader.getMatchingEntries(categories, latestOnly);

        Map<String, List<DownloadResult>> result = new HashMap<>();
        if (matchingEntries.isEmpty()) {
            logger.warning("No entries found to download for specified categories " + categories);
        } else {
            for (String category : matchingEntries.keySet()) {
                List<DownloadResult> downloads = new ArrayList<>();
                for (Entry entry : matchingEntries.get(category)) {
                    downloads.add(downloader.downloadEntry(entry, outputDirectory));
                }
                result.put(category, downloads);
            }
        }

        return result;
    }
}
