package org.araymond.joal.core.config;

/**
 * Created by raymo on 24/01/2017.
 */
public class AppConfigurationIntegrityException extends RuntimeException {
    private static final long serialVersionUID = 2653545407314227748L;

    AppConfigurationIntegrityException() {
    }

    AppConfigurationIntegrityException(final String message) {
        super(message);
    }

    AppConfigurationIntegrityException(final String message, final Throwable cause) {
        super(message, cause);
    }

    AppConfigurationIntegrityException(final Throwable cause) {
        super(cause);
    }

    AppConfigurationIntegrityException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
