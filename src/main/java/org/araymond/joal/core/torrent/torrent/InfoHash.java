package org.araymond.joal.core.torrent.torrent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@ToString
@EqualsAndHashCode(of = "infoHash")
@Getter
public class InfoHash {
    private final String infoHash;
    private final String humanReadable;

    private static final Pattern INVISIBLE_CTRL_CHARS_PTRN = Pattern.compile("\\p{C}");

    public InfoHash(final byte[] bytes) {
        this.infoHash = new String(bytes, MockedTorrent.BYTE_ENCODING);
        this.humanReadable = INVISIBLE_CTRL_CHARS_PTRN.matcher(infoHash).replaceAll(EMPTY);
    }

    public String value() {
        return infoHash;
    }
}
