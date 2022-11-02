package org.araymond.joal.core.exception;

/**
 * Created by raymo on 19/07/2017.
 */
public class UnrecognizedClientPlaceholder extends RuntimeException {
    private static final long serialVersionUID = -1124693366305120032L;

    public UnrecognizedClientPlaceholder(final String message) {
        super(message);
    }
}
