package au.gov.digitalhealth.ncts.syndication.client.exception;

/**
 * Exception thrown when two entries have mismatching content item identifiers
 */
public class MismatchingContentItemIdentifierException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MismatchingContentItemIdentifierException(String message) {
        super(message);
    }

}
