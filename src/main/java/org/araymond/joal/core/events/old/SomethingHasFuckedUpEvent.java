package org.araymond.joal.core.events.old;

import com.google.common.base.Preconditions;

/**
 * Created by raymo on 11/05/2017.
 */
public class SomethingHasFuckedUpEvent {

    private final Throwable exception;

    public SomethingHasFuckedUpEvent(final Throwable exception) {
        Preconditions.checkNotNull(exception, "Exception cannot be null.");
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }
}
