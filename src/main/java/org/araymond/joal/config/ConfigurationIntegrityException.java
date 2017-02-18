package org.araymond.joal.config;

/**
 * Created by raymo on 24/01/2017.
 */
public class ConfigurationIntegrityException extends RuntimeException {
    private static final long serialVersionUID = 2653545407314227748L;

    ConfigurationIntegrityException() {
    }

    ConfigurationIntegrityException(final String message) {
        super(message);
    }

    ConfigurationIntegrityException(final String message, final Throwable cause) {
        super(message, cause);
    }

    ConfigurationIntegrityException(final Throwable cause) {
        super(cause);
    }

    ConfigurationIntegrityException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
