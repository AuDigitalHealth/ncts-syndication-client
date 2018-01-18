package com.github.dionmcm.ncts.syndication.client;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Downloads the latest version of a syndication artefact
 */
@Mojo(name = "download-syndication-artefact")
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
    Set<String> categories;

    @Parameter(defaultValue = "true")
    boolean latestOnly;

    @Parameter(required = true)
    String clientId;

    @Parameter(required = true)
    String clientSecret;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            NctsFeedReader feedReader = new NctsFeedReader(feedUrl);
            NctsFileDownloader downloader = new NctsFileDownloader(new URI(tokenUrl), clientId, clientSecret);

            Map<String, Set<Entry>> matchingEntries = feedReader.getMatchingEntries(categories, latestOnly);

            if (matchingEntries.isEmpty()) {
                getLog().warn("No entries found to download for specified categories " + categories);
            } else {
                for (String category : matchingEntries.keySet()) {
                    for (Entry entry : matchingEntries.get(category)) {
                        downloader.downloadEntry(entry, outputDirectory);
                    }
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Failed reading syndication feed", e);
        }
    }
}
