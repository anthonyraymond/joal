package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.core.sharing.domain.exceptions.InvalidContributionException;

public record UploadAmount(long bytes) {
    public UploadAmount {
        if (bytes < 0) throw new InvalidContributionException("UploadAmount must be greater or equal to 0, received [%d]".formatted(bytes));
    }

    UploadAmount plus(UploadAmount uploadAmount) {
        return new UploadAmount(this.bytes + uploadAmount.bytes);
    }
}
