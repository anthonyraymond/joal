package org.araymond.joal.core.bandwith;

public class Speed {
    private long bytesPerSeconds;

    Speed(final long bytesPerSeconds) {
        this.bytesPerSeconds = bytesPerSeconds;
    }

    public long getBytesPerSeconds() {
        return bytesPerSeconds;
    }

    void setBytesPerSeconds(final long bytesPerSeconds) {
        this.bytesPerSeconds = bytesPerSeconds;
    }
}
