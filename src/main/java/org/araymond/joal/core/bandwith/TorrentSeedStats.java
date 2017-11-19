package org.araymond.joal.core.bandwith;

public class TorrentSeedStats {

    private long uploaded;

    void addUploaded(final long bytes) {
        this.uploaded += bytes;
    }

    public long getUploaded() {
        return uploaded;
    }

    public long getDownloaded() {
        return 0L;
    }

    public long getLeft() {
        return 0L;
    }
}
