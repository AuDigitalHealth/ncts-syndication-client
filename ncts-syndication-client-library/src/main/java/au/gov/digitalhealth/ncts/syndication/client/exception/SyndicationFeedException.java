package au.gov.digitalhealth.ncts.syndication.client.exception;

public class SyndicationFeedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SyndicationFeedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyndicationFeedException(String message) {
        super(message);
    }

}
