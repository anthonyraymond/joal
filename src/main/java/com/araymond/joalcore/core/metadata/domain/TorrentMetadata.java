package com.araymond.joalcore.core.metadata.domain;

import com.araymond.joalcore.core.infohash.domain.InfoHash;

public class TorrentMetadata {
    private final InfoHash infoHash;
    private final long totalSize;

    public TorrentMetadata(InfoHash infoHash, long totalSize) {
        this.infoHash = infoHash;
        this.totalSize = totalSize;
    }

    public InfoHash infoHash() {
        return infoHash;
    }

    public long totalSize() {
        return totalSize;
    }
}
