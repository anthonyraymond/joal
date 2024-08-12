package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;
import com.araymond.joalcore.core.sharing.domain.exception.InvalidContributionAmountException;

@ValueObject
public record Left(long bytes) {
    public static final Left ZERO = new Left(0);

    public Left {
        if (bytes < 0) {
            throw new InvalidContributionAmountException("Left amount cannot be negative");
        }
    }

    public Left minus(DownloadAmount download) {
        return new Left(this.bytes - download.bytes());
    }

    public boolean isZero() {
        return bytes == 0;
    }
}
