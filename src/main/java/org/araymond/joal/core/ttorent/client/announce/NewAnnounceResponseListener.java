package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.common.Peer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.util.EventListener;
import java.util.List;

/**
 * Created by raymo on 14/05/2017.
 */
public interface NewAnnounceResponseListener extends EventListener {

    /**
     * Handle an announce response event.
     *
     * @param interval The announce interval requested by the tracker.
     * @param complete The number of seeders on this torrent.
     * @param incomplete The number of leechers on this torrent.
     */
    public void handleAnnounceResponse(final TorrentWithStats torrent, int interval, int complete, int incomplete);

    /**
     * Handle the discovery of new peers.
     *
     * @param peers The list of peers discovered (from the announce response or
     * any other means like DHT/PEX, etc.).
     */
    public void handleDiscoveredPeers(final TorrentWithStats torrent, final List<Peer> peers);
}
