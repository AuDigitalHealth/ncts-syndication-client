package au.gov.digitalhealth.ncts.syndication.client;

import java.util.Comparator;

import au.gov.digitalhealth.ncts.syndication.client.exception.MismatchingContentItemIdentifierException;
import au.gov.digitalhealth.ncts.syndication.client.exception.MismatchingEntryVersionFormatException;
import de.skuzzle.semantic.Version;
import de.skuzzle.semantic.Version.VersionFormatException;

/**
 * Comparator for {@link Entry} objects from an NCTS compatible Atom feed.
 * <p>
 * Entries must have the same content item identifier and must have matching version string formats. Supported version
 * string formats are
 * <ul>
 * <li>a natural number (positive integer)</li>
 * <li>a semantic version - see <a href="https://semver.org/">https://semver.org/</a></li>
 * <li>a SNOMED CT version URI - see
 * <a href="https://confluence.ihtsdotools.org/display/DOCURI/2.1+URIs+for+Editions+and+Versions">https://confluence.
 * ihtsdotools.org/display/DOCURI/2.1+URIs+for+Editions+and+Versions</a></li>
 * </ul>
 * Note that SNOMED CT version URIs for both entries must have the same module identifier.
 */
public class NctsEntryVersionComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {
        String o1VersionString = o1.getContentItemVersion();
        String o2VersionString = o2.getContentItemVersion();

        if (!o1.getContentItemIdentifier().equals(o2.getContentItemIdentifier())) {
            throw new MismatchingContentItemIdentifierException(
                "Cannot compare entries with mismatching content item identifiers "
                    + o1.getContentItemIdentifier() + " and " + o2.getContentItemIdentifier() + " for entries " + o1
                    + " and " + o2);
        }

        if (o1VersionString.matches(NctsFeedReader.SNOMED_VERSION_REGEXP)) {
            String o1Module = o1VersionString.replaceFirst(NctsFeedReader.SNOMED_VERSION_REGEXP, "$1");
            o1VersionString = o1VersionString.replaceFirst(NctsFeedReader.SNOMED_VERSION_REGEXP, "$2");
            if (!o2VersionString.matches(NctsFeedReader.SNOMED_VERSION_REGEXP)) {
                throw new MismatchingEntryVersionFormatException(
                        "Comparing two entries versions from the feed, one is a SNOMED CT "
                                + "version URI, the other isn't. Entries were " + o1 + " and " + o2);
            }

            String o2Module = o2VersionString.replaceFirst(NctsFeedReader.SNOMED_VERSION_REGEXP, "$1");

            if (!o1Module.equals(o2Module)) {
                throw new MismatchingEntryVersionFormatException(
                        "Comparing two entries versions from the feed with SNOMED CT "
                                + "version URIs with mismatching modules " + o1Module + " and " + o2Module
                                + ". Entries were " + o1 + " and " + o2);
            }
            o2VersionString = o2VersionString.replaceFirst("http://snomed.info/sct/\\d+/version/(\\d+)", "$1");
        }

        if (o1VersionString.matches("\\d+") || o2VersionString.matches("\\d+")) {
            if (!(o1VersionString.matches("\\d+") && o2VersionString.matches("\\d+"))) {
                throw new MismatchingEntryVersionFormatException(
                    "One entry format is a number the other is not, entry versions are " + o1.getContentItemVersion()
                            + " and " + o2.getContentItemVersion() + " entries were " + o1 + " and " + o2);
            }
            else {
                return o1VersionString.compareTo(o2VersionString);
            }
        }

        try {
            Version o1Version = Version.parseVersion(o1VersionString);
            Version o2Version = Version.parseVersion(o2VersionString);
            return o1Version.compareTo(o2Version);
        } catch (VersionFormatException e) {
            throw new UnsupportedVersionFormatException(
                    "Latest entry cannot be determined, version strings for entries are not pure numbers"
                        + " (string of digits), SNOMED CT verison URIs or semantic versioning. Entries were " + o1
                        + " and " + o2,
                    e);
        }
    }
}