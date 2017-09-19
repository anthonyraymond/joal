package org.araymond.joal.core.events;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 05/05/2017.
 */
public class NoMoreLeechersEvent {

    private final MockedTorrent torrent;

    public NoMoreLeechersEvent(final MockedTorrent torrent) {
        Preconditions.checkNotNull(torrent, "MockedTorrent cannot be null.");
        this.torrent = torrent;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }
}
