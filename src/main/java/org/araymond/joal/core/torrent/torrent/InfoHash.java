package org.araymond.joal.core.torrent.torrent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "infoHash")
@Getter
public class InfoHash {
    private final String infoHash;
    private final String humanReadable;

    public InfoHash(final byte[] bytes) {
        this.infoHash = new String(bytes, MockedTorrent.BYTE_ENCODING);
        this.humanReadable = infoHash.replaceAll("\\p{C}", "");
    }

    public String value() {
        return infoHash;
    }
}
