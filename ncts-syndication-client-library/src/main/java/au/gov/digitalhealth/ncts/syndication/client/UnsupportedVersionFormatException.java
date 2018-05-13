package au.gov.digitalhealth.ncts.syndication.client;

import de.skuzzle.semantic.Version.VersionFormatException;

/**
 * Exception thrown when attempting to compare {@link Entry} versions, but one or both entries' formats are not
 * supported
 */
public class UnsupportedVersionFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedVersionFormatException(String message, VersionFormatException e) {
        super(message, e);
    }

}
