package org.araymond.joal.client.emulated;

/**
 * Created by raymo on 24/01/2017.
 */
public class TorrentClientConfigIntegrityException extends RuntimeException {
    private static final long serialVersionUID = -2441989395992766363L;

    TorrentClientConfigIntegrityException() {
    }

    TorrentClientConfigIntegrityException(final String message) {
        super(message);
    }

    TorrentClientConfigIntegrityException(final String message, final Throwable cause) {
        super(message, cause);
    }

    TorrentClientConfigIntegrityException(final Throwable cause) {
        super(cause);
    }

    TorrentClientConfigIntegrityException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
