package com.araymond.joalcore.core.sharing.domain.exception;

import java.io.Serial;

public class InvalidChangeOfStateException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 3903648698616160092L;

    public InvalidChangeOfStateException(String message) {
        super(message);
    }
}
