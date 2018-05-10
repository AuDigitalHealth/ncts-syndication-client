package au.gov.digitalhealth.ncts.syndication.client;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Downloads the latest version of a syndication artefact
 */
@Mojo(name = "download-syndication-artefact", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class DownloadSyndicationArtefactMojo extends AbstractMojo {
    public static final String TOKEN_URL = "https://api.healthterminologies.gov.au/oauth2/token";
    public static final String FEED_URL = "https://api.healthterminologies.gov.au/syndication/v1/syndication.xml";

    @Parameter(property = "synd.url", defaultValue = FEED_URL)
    String feedUrl;

    @Parameter(property = "synd.token.url", defaultValue = TOKEN_URL)
    String tokenUrl;

    @Parameter(required = true)
    File outputDirectory;

    @Parameter(required = true)
    String[] categories;

    @Parameter(defaultValue = "true")
    boolean latestOnly;

    @Parameter(required = true)
    String clientId;

    @Parameter(required = true)
    String clientSecret;

    @Override
    public void execute() throws MojoExecutionException {
        try {

            SyndicationClient client = new SyndicationClient(feedUrl, tokenUrl, outputDirectory, clientId,
                    clientSecret);

            client.download(latestOnly, categories);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed reading syndication feed", e);
        }
    }
}
