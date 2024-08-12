package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;

@ValueObject
public record Contribution(DownloadAmount downloaded, UploadAmount uploaded) {
    public static final Contribution ZERO = new Contribution(new DownloadAmount(0), new UploadAmount(0));

    public Contribution add(DownloadAmount downloaded) {
        return new Contribution(this.downloaded().plus(downloaded), this.uploaded());
    }

    public Contribution add(UploadAmount upload) {
        return new Contribution(this.downloaded(), this.uploaded().plus(upload));
    }
}
