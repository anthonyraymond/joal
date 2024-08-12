package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;
import com.araymond.joalcore.annotations.VisibleForTesting;

import java.util.Objects;

@ValueObject
public final class Contributions {
    private final Contribution overall;
    private final Contribution session;
    private final Left left;

    public Contributions(Contribution overall, Left left) {
        this(overall, Contribution.ZERO, left);
    }

    @VisibleForTesting
    Contributions(Contribution overall, Contribution session, Left left) {
        this.overall = overall;
        this.session = session;
        this.left = left;
    }

    public Contributions add(DownloadAmount download) {
        download = download.cappedBy(left);

        return new Contributions(
                this.overall.add(download),
                this.session.add(download),
                this.left.minus(download)
        );
    }

    public Contributions add(UploadAmount upload) {
        return new Contributions(
                this.overall.add(upload),
                this.session.add(upload),
                this.left
        );
    }

    boolean isFullyDownloaded() {
        return this.left.isZero();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Contributions) obj;
        return Objects.equals(this.overall, that.overall) &&
                Objects.equals(this.session, that.session) &&
                Objects.equals(this.left, that.left);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overall, session, left);
    }

    @Override
    public String toString() {
        return "Contributions[" +
                "overall=" + overall + ", " +
                "session=" + session + ", " +
                "left=" + left + ']';
    }
}
