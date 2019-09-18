package au.gov.digitalhealth.ncts.syndication.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import au.gov.digitalhealth.ncts.syndication.client.exception.HashValidationFailureException;
import au.gov.digitalhealth.ncts.syndication.client.exception.SyndicationClientInitialisationException;
import au.gov.digitalhealth.ncts.syndication.client.exception.SyndicationFeedException;

/**
 * Downloads the latest version of a syndication artefact.
 * <p>
 * Construct
 */
public class SyndicationClient {
    private static final Logger logger = Logger.getLogger(SyndicationClient.class.getName());
    public static final String TOKEN_URL = "https://api.healthterminologies.gov.au/oauth2/token";
    public static final String FEED_URL = "https://api.healthterminologies.gov.au/syndication/v1/syndication.xml";

    private URI feedUrl;
    private URI tokenUrl;
    private File outputDirectory;
    private String clientId;
    private String clientSecret;

    /**
     * Constructs a new client defaulting the token URL to {@link #TOKEN_URL} and
     * the feed URL to {@link #FEED_URL} with no authentication.
     * <p>
     * You can use this constructor and the fluent style setter methods instead of
     * the constructors with larger parameter lists.
     */
    public SyndicationClient() {
        this(null, null, null);
        try {
            setOutputDirectory(File.createTempFile("NctsSyndClientDownload", ""));
        } catch (IOException e) {
            throw new SyndicationClientInitialisationException("Cannot create temporary file", e);
        }
    }

    /**
     * Constructs a new client defaulting the token URL to {@link #TOKEN_URL} and
     * the feed URL to {@link #FEED_URL} with no authentication.
     * 
     * @param outputDirectory directory to download resources to
     */
    public SyndicationClient(File outputDirectory) {
        this(outputDirectory, null, null);
    }

    /**
     * Constructs a new client defaulting the token URL to {@link #TOKEN_URL} and
     * the feed URL to {@link #FEED_URL}.
     * <p>
     * Use {@link #SyndicationClient(File)} if no authentication is required.
     * 
     * @param outputDirectory directory to download resources to
     * @param clientId clientID for authentication
     * @param clientSecret client secret for authentication
     */
    public SyndicationClient(File outputDirectory, String clientId, String clientSecret) {
        this(FEED_URL, TOKEN_URL, outputDirectory, clientId, clientSecret);
    }

    /**
     * Constructs a new client.
     * 
     * @param feedUrl URL of the syndication feed
     * @param tokenUrl URL to authenticate against
     * @param outputDirectory directory to download resources to
     * @param clientId clientID for authentication - null if no authentication is required
     * @param clientSecret client secret for authentication - null if no authentication is required
     */
    public SyndicationClient(String feedUrl, String tokenUrl, File outputDirectory, String clientId,
            String clientSecret) {
        super();
        setFeedUrl(feedUrl);
        setTokenUrl(tokenUrl);
        setOutputDirectory(outputDirectory);
        setClientId(clientId);
        setClientSecret(clientSecret);
    }

    /**
     * Downloads the specified categories artefacts to the client's download
     * directory specified in the client's constructor. If files are already present
     * in the download directory the SHA256 will be tested, and if the local copy's
     * SHA256 does not match the feed the item will be downloaded over the top of
     * the existing local copy. SHA256 hashes are tested for each download and if
     * they fail an exception is thrown and the client aborts.
     * 
     * @param latestOnly if true only the latest artefact version from each
     *            specified category will be downloaded, otherwise all
     *            artefacts for each category will be downloaded
     * @param categories syndication feed categories to download, refer to
     *            https://www.healthterminologies.gov.au/specs/v2/conformant-server-apps/syndication-api/syndication-
     *            feed
     * @return a Map containing all the requests categories and a List of
     *         {@link DownloadResult}s, one for each aretfact in the feed matching
     *         the categories provided and latestOnly setting
     * 
     * @throws IOException if an error occurs trying to get the
     *             feed or its contents
     * @throws NoSuchAlgorithmException if the SHA256 algorithm can't be
     *             loaded
     * @throws HashValidationFailureException if the downloaded file's SHA256
     *             doesn't match the hash specified in
     *             the feed
     */
    public Map<String, List<DownloadResult>> download(boolean latestOnly, String... categories)
            throws IOException, NoSuchAlgorithmException, HashValidationFailureException {
        NctsFeedReader feedReader = new NctsFeedReader(feedUrl.toString());
        NctsFileDownloader downloader = new NctsFileDownloader(tokenUrl, clientId, clientSecret);

        Map<String, Set<Entry>> matchingEntries = feedReader.getMatchingEntries(latestOnly, categories);

        Map<String, List<DownloadResult>> result = new HashMap<>();
        if (matchingEntries.isEmpty()) {
            logger.warning(() -> "No entries found to download for specified categories " + categories);
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

    /**
     * Downloads the specified categories artefacts to the client's download
     * directory specified in the client's constructor. If files are already present
     * in the download directory the SHA256 will be tested, and if the local copy's
     * SHA256 does not match the feed the item will be downloaded over the top of
     * the existing local copy. SHA256 hashes are tested for each download and if
     * they fail an exception is thrown and the client aborts.
     * <p>
     * Entries must have a category that matches one of the supplied categories, and a
     * contentItemIdentifier that matches one of the supplied contentItemId values.
     *
     * @param categories syndication feed categories to download, refer to
     *            https://www.healthterminologies.gov.au/specs/v2/conformant-server-apps/syndication-api/syndication-
     *            feed
     * @param categories contentItemIdentifier values to download, refer to
     *            https://www.healthterminologies.gov.au/specs/v2/conformant-server-apps/syndication-api/syndication-
     *            feed
     * @param latestOnly if true only the latest artefact version from each
     *            specified category will be downloaded, otherwise all
     *            artefacts for each category will be downloaded
     * @return a Map containing all the requests categories and a List of
     *         {@link DownloadResult}s, one for each aretfact in the feed matching
     *         the categories provided and latestOnly setting
     *
     * @throws IOException if an error occurs trying to get the
     *             feed or its contents
     * @throws NoSuchAlgorithmException if the SHA256 algorithm can't be
     *             loaded
     * @throws HashValidationFailureException if the downloaded file's SHA256
     *             doesn't match the hash specified in
     *             the feed
     */
    public Map<String, List<DownloadResult>> downloadByCategoryAndContentItemId(
        List<String> categories, List<String> contentItemIds, boolean latestOnly)
        throws IOException, HashValidationFailureException, NoSuchAlgorithmException {
        NctsFeedReader feedReader = new NctsFeedReader(feedUrl.toString());
        NctsFileDownloader downloader = new NctsFileDownloader(tokenUrl, clientId, clientSecret);

        Map<String, Set<Entry>> matchingEntries = feedReader.getMatchingEntriesByContentItemId(
            latestOnly, categories, contentItemIds);

        Map<String, List<DownloadResult>> result = new HashMap<>();
        if (matchingEntries.isEmpty()) {
            logger.warning(() -> "No entries found to download for specified categories " + categories);
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

    /**
     * Convenience method to download the latest file for the specified categories.
     * <p>
     * See {@link #download(boolean, String...)} for more details.
     * 
     * @param categories syndication feed categories to download, refer to
     *            https://www.healthterminologies.gov.au/specs/v2/conformant-server-apps/syndication-api/syndication-
     *            feed
     * @return {@link Map} containing one {@link DownloadResult} for each specified
     *         category.
     * 
     * @throws IOException if an error occurs trying to get the
     *             feed or its contents
     * @throws NoSuchAlgorithmException if the SHA256 algorithm can't be
     *             loaded
     * @throws HashValidationFailureException if the downloaded file's SHA256
     *             doesn't match the hash specified in
     *             the feed
     */
    public Map<String, DownloadResult> downloadLatestFromCategories(String... categories)
            throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        Map<String, DownloadResult> result = new HashMap<>();
        Map<String, List<DownloadResult>> downloadResults = download(true, categories);
        for (String category : downloadResults.keySet()) {
            if (downloadResults.get(category).size() != 1) {
                throw new SyndicationFeedException("Expected only 1 result for category " + category
                        + " but encountered " + downloadResults.get(category).size());
            }

            result.put(category, downloadResults.get(category).get(0));
        }

        return result;
    }

    /**
     * Convenience method to download the latest file for the specified single
     * category.
     * <p>
     * See {@link #download(boolean, String...)} for more details.
     * 
     * @param category syndication feed catagory to download, refer to
     *            https://www.healthterminologies.gov.au/specs/v2/conformant-server-apps/syndication-api/syndication-
     *            feed
     * @return a single {@link DownloadResult} for the requested latest file in the
     *         category
     * @throws IOException if an error occurs trying to get the
     *             feed or its contents
     * @throws NoSuchAlgorithmException if the SHA256 algorithm can't be
     *             loaded
     * @throws HashValidationFailureException if the downloaded file's SHA256
     *             doesn't match the hash specified in
     *             the feed
     */
    public DownloadResult downloadLatest(String category)
            throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        Map<String, DownloadResult> downloadResults = downloadLatestFromCategories(category);
        if (downloadResults.keySet().size() != 1 || !downloadResults.keySet().iterator().next().equals(category)) {
            throw new SyndicationFeedException(
                "Expected only 1 category " + category + " but encountered " + downloadResults.keySet());
        }

        return downloadResults.get(category);
    }

    public URI getFeedUrl() {
        return feedUrl;
    }

    public SyndicationClient setFeedUrl(String feedUrl) {
        return setFeedUrl(URI.create(feedUrl));
    }

    public SyndicationClient setFeedUrl(URI feedUrl) {
        this.feedUrl = feedUrl;
        return this;
    }

    public URI getTokenUrl() {
        return tokenUrl;
    }

    public SyndicationClient setTokenUrl(String tokenUrl) {
        return setTokenUrl(URI.create(tokenUrl));
    }

    public SyndicationClient setTokenUrl(URI tokenUrl) {
        this.tokenUrl = tokenUrl;
        return this;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public SyndicationClient setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public SyndicationClient setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public SyndicationClient setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
}
