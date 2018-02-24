package org.araymond.joal.core.exception;

/**
 * Created by raymo on 19/07/2017.
 */
public class UnrecognizedAnnounceParameter extends RuntimeException {
    private static final long serialVersionUID = -1124693366305120032L;

    public UnrecognizedAnnounceParameter(final String message) {
        super(message);
    }

}
