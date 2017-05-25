package org.araymond.joal.core.exception;

/**
 * Created by raymo on 14/05/2017.
 */
public class NoMoreTorrentsFileAvailableException extends Exception {
    private static final long serialVersionUID = -2114301657174632211L;

    public NoMoreTorrentsFileAvailableException(final String message) {
        super(message);
    }
}
