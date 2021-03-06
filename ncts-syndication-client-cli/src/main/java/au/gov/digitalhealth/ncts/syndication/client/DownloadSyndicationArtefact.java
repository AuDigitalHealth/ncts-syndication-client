package au.gov.digitalhealth.ncts.syndication.client;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import au.gov.digitalhealth.ncts.syndication.client.exception.HashValidationFailureException;

/**
 * Command line executable class that downloads entries from the NCTS syndication feed based on the specified categories
 * of entries to download
 */
public class DownloadSyndicationArtefact {

    private static final String HELP_OPTION = "?";
    private static final String CLIENT_SECRET_OPTION = "secret";
    private static final String CLIENT_ID_OPTION = "id";
    private static final String LATEST_ONLY_OPTION = "latest";
    private static final String CATEGORY_OPTION = "category";
    private static final String OUTPUT_DIRECTORY_OPTION = "out";
    private static final String TOKEN_URL_OPTION = "token";
    private static final String FEED_URL_OPTION = "feed";

    static SyndicationClient client = new SyndicationClient();

    /**
     * Private constructor - this is intended to be a main method only class
     */
    private DownloadSyndicationArtefact() {}

    public static void main(String[] args)
            throws NoSuchAlgorithmException, IOException, HashValidationFailureException {
        String feedUrl;
        String tokenUrl;
        File outputDirectory;
        String[] categories;
        boolean latestOnly;
        String clientId;
        String clientSecret;

        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(HELP_OPTION)) {
                printHelp(options);
            } else {

                feedUrl = line.getOptionValue(FEED_URL_OPTION, SyndicationClient.FEED_URL);
                tokenUrl = line.getOptionValue(TOKEN_URL_OPTION, SyndicationClient.TOKEN_URL);
                if (line.hasOption(OUTPUT_DIRECTORY_OPTION)) {
                    outputDirectory = new File(line.getOptionValue(OUTPUT_DIRECTORY_OPTION));
                } else {
                    outputDirectory = new File(System.getProperty("user.dir"));
                }
                categories = line.getOptionValues(CATEGORY_OPTION);
                latestOnly = line.hasOption(LATEST_ONLY_OPTION);
                clientId = line.getOptionValue(CLIENT_ID_OPTION, null);
                clientSecret = line.getOptionValue(CLIENT_SECRET_OPTION, null);

                client.setFeedUrl(feedUrl)
                    .setTokenUrl(tokenUrl)
                    .setOutputDirectory(outputDirectory)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret);

                client.download(latestOnly, categories);
            }
        } catch (ParseException exp) {
            System.err.println("Invalid arguments:" + exp.getMessage());
            printHelp(options);
            throw new IllegalArgumentException("Invalid arguments", exp);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java " + DownloadSyndicationArtefact.class.getName(), options);
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(
            Option.builder(HELP_OPTION)
                .longOpt("help")
                .desc("Prints the parameter usage help")
                .required(false)
                .build());

        options.addOption(
            Option.builder(FEED_URL_OPTION)
                .longOpt("feed-url")
                .desc(
                    "URL of the NCTS syndication feed, defaults to https://api.healthterminologies.gov.au/syndication/v1/syndication.xml if not set")
                .hasArg()
                .required(false)
                .build());

        options.addOption(
            Option.builder(TOKEN_URL_OPTION)
                .longOpt("token-url")
                .desc(
                    "URL of the NCTS token endpoint for authentication, defaults to https://api.healthterminologies.gov.au/oauth2/token if not set")
                .hasArg()
                .required(false)
                .build());

        options.addOption(
            Option.builder(OUTPUT_DIRECTORY_OPTION)
                .longOpt("output-directory")
                .desc(
                    "Directory to download entries to")
                .hasArg()
                .required(false)
                .build());

        options.addOption(
            Option.builder(CATEGORY_OPTION)
                .desc(
                    "Category to download entries for, these appear in the term attribute of the category elements in the feed, for example 'SCT_RF2_FULL'")
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .required(true)
                .build());

        options.addOption(
            Option.builder(LATEST_ONLY_OPTION)
                .desc(
                    "If provided, this option will cause the process to only download the latest entry (entry with the biggest content item version) from the feed for each category specified")
                .hasArg(false)
                .required(false)
                .build());

        options.addOption(
            Option.builder(CLIENT_ID_OPTION)
                .longOpt("client-id")
                .desc(
                    "Client id from the client credentials to use when authenticating to download entries")
                .hasArg()
                .build());

        options.addOption(
            Option.builder(CLIENT_SECRET_OPTION)
                .longOpt("client-secret")
                .desc(
                    "Secret for the client id specified from the client credentials to use when authenticating to download entries")
                .hasArg()
                .build());
        return options;
    }
}
