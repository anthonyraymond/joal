package org.araymond.joal.core.torrent.torrent;

import com.google.common.base.Objects;

public class InfoHash {
    private final String infoHash;

    public InfoHash(final byte[] bytes) {
        this.infoHash = new String(bytes, MockedTorrent.BYTE_ENCODING);
    }

    public String value() {
        return infoHash;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final InfoHash infoHash1 = (InfoHash) o;
        return Objects.equal(infoHash, infoHash1.infoHash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(infoHash);
    }
}
