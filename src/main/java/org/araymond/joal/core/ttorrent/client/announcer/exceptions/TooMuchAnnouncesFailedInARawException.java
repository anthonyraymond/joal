package org.araymond.joal.core.ttorrent.client.announcer.exceptions;

import lombok.Getter;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

@Getter
public class TooManyAnnouncesFailedInARowException extends Exception {
    private static final long serialVersionUID = 1864953989056739188L;

    private final MockedTorrent torrent;

    public TooManyAnnouncesFailedInARowException(final MockedTorrent torrent) {
        super();
        this.torrent = torrent;
    }
}
