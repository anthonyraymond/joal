package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.torrent.torrent.InfoHash;

public class NewBandwidthDispatcher {

    public TorrentSeedStats getSeedStatForTorrent(final InfoHash infoHash) {
        return null;
    }

    public void updateTorrentPeers(final InfoHash torrent, final int seeders, final int leechers) {
    }

    public void registerTorrent(final InfoHash infoHash) {
    }
    
    public void unregisterTorrent(final InfoHash infoHash) {
    }

}
