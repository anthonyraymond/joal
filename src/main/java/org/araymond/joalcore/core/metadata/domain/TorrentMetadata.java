package org.araymond.joalcore.core.metadata.domain;

import java.util.Objects;

public record TorrentMetadata(InfoHash infoHash, TorrentSize size) {
    public TorrentMetadata {
        Objects.requireNonNull(infoHash, "TorrentMetadata requires a non-null [infoHash]");
        Objects.requireNonNull(size, "TorrentMetadata requires a non-null [size]");
    }
}
