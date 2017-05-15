package org.araymond.joal.core.exception;

/**
 * Created by raymo on 14/05/2017.
 */
public class NoMoreTorrentsFileAvailableException extends Exception {
    private static final long serialVersionUID = -2114301657174632211L;

    public NoMoreTorrentsFileAvailableException() {
    }

    public NoMoreTorrentsFileAvailableException(String message) {
        super(message);
    }

    public NoMoreTorrentsFileAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMoreTorrentsFileAvailableException(Throwable cause) {
        super(cause);
    }

    public NoMoreTorrentsFileAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
