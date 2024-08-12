package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.core.sharing.domain.exceptions.InvalidContributionAmountException;

public record DownloadAmount(long bytes) {
    public DownloadAmount {
        if (bytes < 0) throw new InvalidContributionAmountException("DownloadAmount must be greater or equal to 0, received [%d]".formatted(bytes));
    }

    DownloadAmount plus(DownloadAmount downloadAmount) {
        return new DownloadAmount(this.bytes + downloadAmount.bytes);
    }

    DownloadAmount cappedBy(Left left) {
        return new DownloadAmount(Math.min(this.bytes, left.bytes()));
    }
}
