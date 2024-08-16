package org.araymond.joalcore.core.sharing.domain.exceptions;

import java.io.Serial;

public class InvalidContributionException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5796632993921772588L;

    public InvalidContributionException(String message) {
        super(message);
    }
}
