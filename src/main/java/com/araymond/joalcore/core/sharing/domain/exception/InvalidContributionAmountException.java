package com.araymond.joalcore.core.sharing.domain.exception;

import java.io.Serial;

public class InvalidContributionAmountException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5796632993921772588L;

    public InvalidContributionAmountException(String message) {
        super(message);
    }
}
