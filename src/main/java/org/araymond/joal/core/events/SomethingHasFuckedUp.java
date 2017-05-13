package org.araymond.joal.core.events;

/**
 * Created by raymo on 11/05/2017.
 */
public class SomethingHasFuckedUp {

    private final Throwable exception;

    public SomethingHasFuckedUp(final Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }
}
