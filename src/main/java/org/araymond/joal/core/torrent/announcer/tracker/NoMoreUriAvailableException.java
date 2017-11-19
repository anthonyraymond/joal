package org.araymond.joal.core.torrent.announcer.tracker;

public class NoMoreUriAvailableException extends Exception {
    private static final long serialVersionUID = 2596973075142465090L;

    public NoMoreUriAvailableException(final String message) {
        super(message);
    }
}
