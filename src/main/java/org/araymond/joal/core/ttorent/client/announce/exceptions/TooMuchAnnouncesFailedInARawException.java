package org.araymond.joal.core.ttorent.client.announce.exceptions;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;

public class TooMuchAnnouncesFailedInARawException extends Exception {
    private static final long serialVersionUID = 1864953989056739188L;

    private final MockedTorrent torrent;

    public TooMuchAnnouncesFailedInARawException(final MockedTorrent torrent) {
        super();
        this.torrent = torrent;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }
}
