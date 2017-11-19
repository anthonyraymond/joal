package org.araymond.joal.core.events.filechange;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

/**
 * Created by raymo on 06/05/2017.
 */
public class TorrentFileAddedEvent {
    private final MockedTorrent torrent;

    public TorrentFileAddedEvent(final MockedTorrent torrent) {
        Preconditions.checkNotNull(torrent, "MockedTorrent cannot be null.");
        this.torrent = torrent;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }
}
