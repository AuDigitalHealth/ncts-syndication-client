package au.gov.digitalhealth.ncts.syndication.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import au.gov.digitalhealth.ncts.syndication.client.exception.AuthenticationException;
import au.gov.digitalhealth.ncts.syndication.client.exception.HashValidationFailureException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Class that reads an NCTS Atom feed and presents it as {@link Entry} objects
 * organised by category.
 */
public class NctsFileDownloader {

    private static final Logger logger = Logger.getLogger(NctsFileDownloader.class.getName());

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String token;
    private URI tokenUrl;
    private String clientId;
    private String clientSecret;

    public NctsFileDownloader(URI tokenUrl, String clientId, String clientSecret) {
        super();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Downloads the specified {@link Entry} from a {@link NctsFeedReader} to the
     * specified directory.
     * <p>
     * Files will be downloaded into a subdirectory with the name of the category
     * they are from and with the filename from the URL the {@link Entry} is at.
     * <p>
     * If a file already exists it will be checked against the SHA256 and length
     * from the entry in the feed. If it matches this download will be skipped. If
     * it does not match, a warning is logged and the file is deleted and downloaded
     * again - this can be useful in cases where a download was killed and a partial
     * file remains.
     * <p>
     * If the file doesn't exist at all it will be downloaded.
     * <p>
     * Once the file is downloaded, the SHA256 and length is checked against the
     * details in the {@link Entry} from the feed. If the SHA256 or length don't
     * match the file will be deleted (to prevent its use) and an exception is
     * thrown.
     * 
     * @param entry           the {@link Entry} to download
     * @param outputDirectory the base directory to download to, the {@link Entry}
     *                        will be downloaded to a directory named the same as
     *                        the category the entry is in and the filename will be
     *                        the same as the filename in the entry url
     * @return DownloadResult indicating the location of the downloaded file and
     *         whether it was downloaded or the locally cached file was up to date
     *         already
     * @throws NoSuchAlgorithmException       if the SHA256 algorithm can't be
     *                                        loaded
     * @throws IOException                    if an error occurs reading the file
     *                                        from the URL or writing it to disk
     * @throws HashValidationFailureException if the downloaded file's SHA256
     *                                        doesn't match the hash specified in
     *                                        the feed
     */
    public DownloadResult downloadEntry(Entry entry, File outputDirectory)
            throws IOException, NoSuchAlgorithmException, HashValidationFailureException {
        File out = getOutputFile(entry, outputDirectory);
        entry.setFile(out);

        if (out.exists() && out.isFile()) {
            if (!sha256AndLengthMatch(entry, out)) {
                logger.warning(() -> "File " + out.getAbsolutePath() + " exists for entry " + entry.getId()
                        + " but does not match feed entry sha256 and/or length - deleting file and redownloading it.");
                if (!out.delete()) {
                    throw new IOException("Unable to delete existing cached file " + out.getAbsolutePath()
                            + " whose sha256 doesn't match the feed. Unable to redownload the file with the corrected sha256");
                }
                downloadFile(entry, out);
                return new DownloadResult(out, true);
            } else {
                logger.info(() -> "File " + out.getAbsolutePath() + " exists for entry " + entry.getId()
                        + " with matching sha256 and length - skipping dowload.");
                return new DownloadResult(out, false);
            }
        } else {
            logger.info(() -> "File " + out.getAbsolutePath() + " does not exists for entry " + entry.getId()
                    + " - starting download for new file.");
            downloadFile(entry, out);
            return new DownloadResult(out, true);
        }

    }

    private void downloadFile(Entry entry, File out)
            throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        HttpClientBuilder builder = HttpClients.custom();
        builder.addInterceptorFirst((HttpRequest request, HttpContext context) -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getBearerTokenFromAuthServer());
        });
        try (DigestOutputStream dos = new DigestOutputStream(new BufferedOutputStream(new FileOutputStream(out)),
                digest);
                CloseableHttpClient httpClient = builder.build();
                CloseableHttpResponse response = httpClient.execute(new HttpGet(entry.getUrl()));) {
            InputStream is = response.getEntity().getContent();
            int inByte;
            while ((inByte = is.read()) != -1) {
                dos.write(inByte);
            }
        }
        String downloadedFileSha256 = Hex.encodeHexString(digest.digest());

        long length = out.length();
        if (!sha256AndLengthMatch(entry, length, downloadedFileSha256)) {
            if (!out.delete()) {
                logger.warning(() -> "Unable to delete downloaded file " + out.getAbsolutePath()
                        + " whose sha256 doesn't match the feed.");
            }
            throw new HashValidationFailureException(out, downloadedFileSha256, length, entry.getSha256(),
                    entry.getLength());
        }
    }

    private File getOutputFile(Entry entry, File outputDirectory) {
        String[] urlParts = entry.getUrl().split("[/]");
        String filename = urlParts[urlParts.length - 1];
        return new File(outputDirectory, filename);
    }

    private boolean sha256AndLengthMatch(Entry entry, File out) throws IOException {
        String existingSha256;
        try (FileInputStream fis = new FileInputStream(out)) {
            existingSha256 = DigestUtils.sha256Hex(fis);
        }

        return sha256AndLengthMatch(entry, out.length(), existingSha256);

    }

    private boolean sha256AndLengthMatch(Entry entry, Long length, String existingSha256) {
        return length == entry.getLength() && existingSha256.equals(entry.getSha256());
    }

    private String getBearerTokenFromAuthServer() {
        if (token == null) {
            try {
                HttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(tokenUrl);
                List<NameValuePair> data = new ArrayList<NameValuePair>();
                data.add(new BasicNameValuePair("grant_type", "client_credentials"));
                data.add(new BasicNameValuePair("client_id", clientId));
                data.add(new BasicNameValuePair("client_secret", clientSecret));
                post.addHeader("Content-Type", "application/x-www-form-urlencoded");
                post.setEntity(new UrlEncodedFormEntity(data, "utf-8"));
                HttpEntity responseEntity = client.execute(post).getEntity();
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> responseMap = gson.fromJson(EntityUtils.toString(responseEntity), type);
                token = responseMap.get("access_token");
            } catch (IOException e) {
                throw new AuthenticationException("Could not get token from authentication server", e);
            }
        }
        return token;
    }
}
