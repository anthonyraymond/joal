package org.araymond.joal.core.config;

/**
 * Created by raymo on 24/01/2017.
 */
public class AppConfigurationIntegrityException extends RuntimeException {
    private static final long serialVersionUID = 2653545407314227748L;

    AppConfigurationIntegrityException(final String message) {
        super(message);
    }
}
