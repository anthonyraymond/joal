package org.araymond.joal.core.events.torrent.files;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

/**
 * Created by raymo on 06/05/2017.
 */
@Getter
public class TorrentFileAddedEvent {
    private final MockedTorrent torrent;

    public TorrentFileAddedEvent(final MockedTorrent torrent) {
        Preconditions.checkNotNull(torrent, "MockedTorrent cannot be null.");
        this.torrent = torrent;
    }
}
