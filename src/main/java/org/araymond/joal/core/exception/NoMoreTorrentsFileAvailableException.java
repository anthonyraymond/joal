package org.araymond.joal.core.exception;

/**
 * Created by raymo on 14/05/2017.
 */
public class NoMoreTorrentsFileAvailableException extends Exception {
    private static final long serialVersionUID = -2114301657174632211L;

    public NoMoreTorrentsFileAvailableException() {
    }

    public NoMoreTorrentsFileAvailableException(final String message) {
        super(message);
    }

    public NoMoreTorrentsFileAvailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NoMoreTorrentsFileAvailableException(final Throwable cause) {
        super(cause);
    }

    public NoMoreTorrentsFileAvailableException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
