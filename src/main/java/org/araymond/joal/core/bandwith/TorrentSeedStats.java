package org.araymond.joal.core.bandwith;

import lombok.Getter;

@Getter
public class TorrentSeedStats {

    private long uploaded;
    private long downloaded;
    private long left;

    void addUploaded(final long bytes) {
        this.uploaded += bytes;
    }
}
