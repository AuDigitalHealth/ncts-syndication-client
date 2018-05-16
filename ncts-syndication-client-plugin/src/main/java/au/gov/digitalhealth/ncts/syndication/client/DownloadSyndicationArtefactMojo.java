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

    @Parameter(property = "synd.url", defaultValue = SyndicationClient.FEED_URL)
    String feedUrl;

    @Parameter(property = "synd.token.url", defaultValue = SyndicationClient.TOKEN_URL)
    String tokenUrl;

    @Parameter(required = true)
    File outputDirectory;

    @Parameter(required = true)
    String[] categories;

    @Parameter(defaultValue = "true")
    boolean latestOnly;

    @Parameter
    String clientId;

    @Parameter
    String clientSecret;

    SyndicationClient client = new SyndicationClient();

    @Override
    public void execute() throws MojoExecutionException {
        try {
            client.setFeedUrl(feedUrl)
                .setTokenUrl(tokenUrl)
                .setOutputDirectory(outputDirectory)
                .setClientId(clientId)
                .setClientSecret(clientSecret);

            client.download(latestOnly, categories);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed reading syndication feed", e);
        }
    }
}
