package au.gov.digitalhealth.ncts.syndication.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import au.gov.digitalhealth.ncts.syndication.client.exception.SyndicationFeedException;

/**
 * Class that reads an NCTS Atom feed and presents it as {@link Entry} objects
 * organised by category.
 */
public class NctsFeedReader {
    static final String SNOMED_VERSION_REGEXP = "http://snomed.info/sct/(\\d+)/version/(\\d+)";
    private static final Logger logger = Logger.getLogger(NctsFeedReader.class.getName());
    private static final Namespace NCTS_NAMESPACE = Namespace
        .getNamespace("http://ns.electronichealth.net.au/ncts/syndication/asf/extensions/1.0.0");
    private static final Namespace ATOM_NAMESPACE = Namespace.getNamespace("http://www.w3.org/2005/Atom");

    private Map<String, Set<Entry>> entries = new HashMap<>();

    /**
     * Constructs a new NCTS feed reader for the specified URL. If the URL cannot be
     * read or the document at that URL cannot be parsed as expected an exception
     * will be thrown.
     * 
     * @param feedUrl the URL of the NCTS syndication feed to read
     * 
     * @throws IOException if the document at the feedUrl cannot be read
     */
    public NctsFeedReader(String feedUrl) throws IOException {
        logger.info(() -> "Initialising NctsFeedReader from feed " + feedUrl);
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = null;
        try {
            doc = saxBuilder.build(feedUrl);
        } catch (JDOMException e) {
            throw new SyndicationFeedException("Cannot parse syndication feed", e);
        }

        Element rootElement = doc.getRootElement();
        List<Element> feedEntries = rootElement.getChildren("entry", ATOM_NAMESPACE);

        for (Element entryElement : feedEntries) {
            List<Element> entryCategoryElements = entryElement.getChildren("category", ATOM_NAMESPACE);

            String id = entryElement.getChildText("id", ATOM_NAMESPACE);

            if (entryCategoryElements.size() != 1) {
                throw new SyndicationFeedException("Entry " + id + " doesn't have exactly one category");
            }

            Element entryCategoryElement = entryCategoryElements.iterator().next();
            Element link = getLink(entryElement);
            Entry entry = new Entry(id, link.getAttributeValue("sha256Hash", NCTS_NAMESPACE),
                link.getAttributeValue("href"), Long.parseLong(link.getAttributeValue("length")),
                entryElement.getChildText("contentItemIdentifier", NCTS_NAMESPACE),
                entryElement.getChildText("contentItemVersion", NCTS_NAMESPACE),
                entryCategoryElement.getAttributeValue("term"), entryCategoryElement.getAttributeValue("scheme"));

            addEntry(entry);
        }

        logger.info(() -> "Feed " + feedUrl + " successfully read");
        entries.keySet().forEach(c -> logger.info("Category " + c + " has " + entries.get(c).size() + " entries"));
    }

    /**
     * Gets the entry with the greatest content item version from the feed in the
     * specified category
     * 
     * @param category required content category for the entry to match
     * @return {@link Entry} from the feed in the specified category with the
     *         biggest content item version
     */
    public Entry getLatestMatchingEntry(String category) {
        return getMatchingEntries(true, category).get(category).iterator().next();
    }

    /**
     * Gets all the entries for a given category
     * 
     * @param category required category for matching entries
     * @return {@link Set} of {@link Entry} objects matching the specified category
     *         in the feed
     */
    public Set<Entry> getMatchingEntries(String category) {
        return Collections.unmodifiableSet(entries.get(category));
    }

    /**
     * Gets {@link Entry} objects from the feed in the specified categories.
     * <p>
     * The response object is a {@link Map} keyed by the categories specified. Each
     * category in the {@link Map} will have a {@link Set} of {@link Entry} objects
     * that are in the feed with that category. If latestOnly is set to true each
     * {@link Set} of {@link Entry} will contain only one {@link Entry} being the
     * one with the largest content item version for the category, otherwise the
     * {@link Set} will contain all {@link Entry} objects for the category.
     * <p>
     * Only categories found to exist in the feed will be returned as keys in the
     * response {@link Map}.
     * 
     * @param categories {@link List} of categories to get from the feed
     * @param latestOnly indicates if only the latest matching {@link Entry} per
     *            category should be returned, if true each {@link Set} of
     *            {@link Entry} for a category will have only one
     *            {@link Entry} otherwise all {@link Entry}s for each
     *            category in the feed will be returned
     * @return {@link Map} keyed by the specified categories containing one
     *         {@link Set} per category containine the matching {@link Entry}
     *         objects. Note that if a category is specified but does not occur in
     *         the feed content the category will not appear as a key in the
     *         returned map.
     */
    public Map<String, Set<Entry>> getMatchingEntries(boolean latestOnly, String... categories) {
        Map<String, Set<Entry>> matchingEntries = new HashMap<>();
        Set<String> categorySet = new HashSet<>(Arrays.asList(categories));

        for (String category : entries.keySet()) {
            if (categorySet.contains(category)) {
                HashSet<Entry> set = new HashSet<>();
                set.addAll(entries.get(category));
                if (latestOnly) {
                    set.retainAll(Arrays.asList(getLatestEntry(set)));
                }
                matchingEntries.put(category, set);
            }
        }

        return matchingEntries;
    }

    private Entry getLatestEntry(HashSet<Entry> set) {
        return set.stream().max(new NctsEntryVersionComparator()).orElseThrow(
            () -> new SyndicationFeedException("No latest entry for set " + set));
    }

    private Element getLink(Element entry) {
        List<Element> links = entry.getChildren("link", ATOM_NAMESPACE);

        if (links.size() != 1) {
            throw new SyndicationFeedException(
                "Entry " + entry.getChild("id", ATOM_NAMESPACE) + " does not have exactly one link");
        }
        return links.iterator().next();
    }

    private void addEntry(Entry entry) {
        Set<Entry> cachedEntries = entries.get(entry.getCategory());
        if (cachedEntries == null) {
            cachedEntries = new HashSet<>();
            entries.put(entry.getCategory(), cachedEntries);
        }

        if (cachedEntries.contains(entry)) {
            throw new SyndicationFeedException("Feed contains duplicate entries for ID " + entry.getId());
        }

        cachedEntries.add(entry);
    }
}
