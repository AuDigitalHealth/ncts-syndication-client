package au.gov.digitalhealth.ncts.syndication.client.exception;

import au.gov.digitalhealth.ncts.syndication.client.Entry;

/**
 * Exception thrown when attempting to compare {@link Entry} versions, but their version formats do not match
 */
public class MismatchingEntryVersionFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MismatchingEntryVersionFormatException(String message) {
        super(message);
    }

}
