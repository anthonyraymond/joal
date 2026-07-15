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

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public InfoHash(final byte[] bytes) {
        this.infoHash = new String(bytes, MockedTorrent.BYTE_ENCODING);
        this.humanReadable = bytesToHex(bytes);
    }

    private static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            final int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String value() {
        return infoHash;
    }
}
