package au.gov.digitalhealth.ncts.syndication.client.exception;

import au.gov.digitalhealth.ncts.syndication.client.SyndicationClient;

/**
 * Thrown if the {@link SyndicationClient} cannot be initialised
 */
public class SyndicationClientInitialisationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SyndicationClientInitialisationException(String message, Exception e) {
        super(message, e);
    }

}
