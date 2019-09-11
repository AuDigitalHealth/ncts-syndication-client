package au.gov.digitalhealth.ncts.syndication.client;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.UUID;

import org.testng.annotations.Test;

import au.gov.digitalhealth.ncts.syndication.client.exception.MismatchingContentItemIdentifierException;
import au.gov.digitalhealth.ncts.syndication.client.exception.MismatchingEntryVersionFormatException;

public class NctsEntryVersionComparatorTest {
    private static final String TEST_GREATER_SCT_VERSION = "http://snomed.info/sct/123/version/20180228";
    private static final String TEST_SCT_VERSION = "http://snomed.info/sct/123/version/20180131";
    private static final String TEST_SCT_VERSION_DIFFERENT_MODULE = "http://snomed.info/sct/321/version/20180131";
    NctsEntryVersionComparator comparator = new NctsEntryVersionComparator();

    @Test(description = "compare entries with SNOMED CT URIs")
    public void compareSctUri() {
        assertEquals(
            comparator.compare(createEntry(TEST_SCT_VERSION),
                createEntry(TEST_SCT_VERSION)),
            0,
            "Zero result expected for equal entries");

        assertTrue(comparator.compare(createEntry(TEST_GREATER_SCT_VERSION),
            createEntry(TEST_SCT_VERSION)) > 0,
            "Positive result expected for greater entries");

        assertTrue(
            comparator.compare(createEntry(TEST_SCT_VERSION),
                createEntry(TEST_GREATER_SCT_VERSION)) < 0,
            "Negative expected for greater entries");
    }

    @Test(description = "compare entries with SNOMED CT URIs with mismatching moduleid", expectedExceptions = {
            MismatchingEntryVersionFormatException.class })
    public void compareSctUriMismatchModule() {
        comparator.compare(createEntry(TEST_SCT_VERSION), createEntry(TEST_SCT_VERSION_DIFFERENT_MODULE));
    }

    @Test(description = "compare entries with version numbers")
    public void compareNumbers() {
        assertEquals(comparator.compare(createEntry("0"), createEntry("0")), 0,
            "Zero result expected for equal entries");

        assertEquals(comparator.compare(createEntry("123"), createEntry("123")), 0,
            "Zero result expected for equal entries");

        assertTrue(comparator.compare(createEntry("1"), createEntry("0")) > 0,
            "Positive result expected for greater entries");

        assertTrue(comparator.compare(createEntry("123"), createEntry("12")) > 0,
            "Positive result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0"), createEntry("1")) < 0,
            "Negative result expected for greater entries");

        assertTrue(comparator.compare(createEntry("12"), createEntry("123")) < 0,
            "Negative result expected for greater entries");
    }

    @Test(description = "compare entries with semantic versions")
    public void compareSemVer() {
        assertEquals(comparator.compare(createEntry("0.1.2"), createEntry("0.1.2")), 0,
            "Zero result expected for equal entries");

        assertTrue(comparator.compare(createEntry("0.1.3"), createEntry("0.1.2")) > 0,
            "Positive result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0.1.20"), createEntry("0.1.2")) > 0,
            "Positive result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0.1.2"), createEntry("0.1.3")) < 0,
            "Negative result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0.1.2"), createEntry("0.1.20")) < 0,
            "Negative result expected for greater entries");
    }

    @Test(description = "compare entries with semantic versions with pre-release tag")
    public void compareSemVerPre() {
        assertEquals(comparator.compare(createEntry("0.1.2-alpha"), createEntry("0.1.2-alpha")), 0,
            "Zero result expected for equal entries");

        assertTrue(comparator.compare(createEntry("0.1.2-beta"), createEntry("0.1.2-alpha")) > 0,
            "Positive result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0.1.2-alpha2"), createEntry("0.1.2-alpha")) > 0,
            "Positive result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0.1.2-alpha"), createEntry("0.1.2-beta")) < 0,
            "Negative result expected for greater entries");

        assertTrue(comparator.compare(createEntry("0.1.2-alpha"), createEntry("0.1.2-alpha2")) < 0,
            "Negative result expected for greater entries");
    }

    @Test(description = "compare entries with semantic versions with pre-release tag and build info")
    public void compareSemVerPreTag() {
        assertEquals(comparator.compare(createEntry("0.1.2-alpha+01"), createEntry("0.1.2-alpha+01")), 0,
            "Zero result expected for equal entries");

        assertEquals(comparator.compare(createEntry("0.1.2-alpha+011"), createEntry("0.1.2-alpha+01")), 0,
            "Zero result expected for equal entries - sem ver tags don't count to precedence");

        assertTrue(comparator.compare(createEntry("0.1.2-alpha2+01"), createEntry("0.1.2-alpha+011")) > 0,
            "Positive result expected for greater entries");

        assertEquals(comparator.compare(createEntry("0.1.2-alpha+01"), createEntry("0.1.2-alpha+011")), 0,
            "Zero result expected for equal entries - sem ver tags don't count to precedence");

        assertTrue(comparator.compare(createEntry("0.1.2-alpha+011"), createEntry("0.1.2-alpha2+01")) < 0,
            "Negative result expected for greater entries");
    }

    @Test(description = "compare entries with unsupported version format", expectedExceptions = {
            UnsupportedVersionFormatException.class })
    public void compareUnsupportedVersionFormat() {
        comparator.compare(createEntry("1.0"), createEntry("1.0.0"));
    }

    @Test(description = "compare entries with mismatching component identifiers", expectedExceptions = {
            MismatchingContentItemIdentifierException.class })
    public void compareMismatchingComponentIdentifier() {
        comparator.compare(createEntry(TEST_SCT_VERSION, "123"), createEntry(TEST_SCT_VERSION, "456"));
    }

    @Test(description = "compare entries with mismatching formats - SCT URI and sem ver", expectedExceptions = {
            MismatchingEntryVersionFormatException.class })
    public void compareMismatchingFormatSctUriAndSemVer() {
        comparator.compare(createEntry(TEST_SCT_VERSION), createEntry("1.2.3"));
    }

    @Test(description = "compare entries with mismatching formats - SCT URI and number", expectedExceptions = {
            MismatchingEntryVersionFormatException.class })
    public void compareMismatchingFormatSctUriAndNumber() {
        comparator.compare(createEntry(TEST_SCT_VERSION), createEntry("123"));
    }

    @Test(description = "compare entries with mismatching formats - SCT URI and number", expectedExceptions = {
            MismatchingEntryVersionFormatException.class })
    public void compareMismatchingFormatNumberAndSemVer() {
        comparator.compare(createEntry("123"), createEntry("1.2.3"));
    }

    private Entry createEntry(String version) {
        return createEntry(version, "contentItemIdentifier");
    }

    private Entry createEntry(String version, String contentItemIdentifier) {
        return new Entry(UUID.randomUUID().toString(), "sha256", "url", 123L, contentItemIdentifier, version,
            "category", "categoryScheme");
    }
}
