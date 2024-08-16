package org.araymond.joalcore.core.metadata.domain;

import org.araymond.joalcore.annotations.ddd.ValueObject;

import java.util.Objects;

@ValueObject
public final class TorrentSize {
    private final long bytes;

    public static TorrentSize ofBytes(long bytes) {
        return new TorrentSize(bytes);
    }

    private TorrentSize(long bytes) {
        this.bytes = bytes;
    }

    public long bytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TorrentSize) obj;
        return this.bytes == that.bytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes);
    }

    @Override
    public String toString() {
        return "TorrentSize[" +
                "bytes=" + bytes + ']';
    }


}
