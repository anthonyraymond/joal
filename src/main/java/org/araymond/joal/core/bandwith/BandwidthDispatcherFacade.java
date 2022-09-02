package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.torrent.torrent.InfoHash;

public interface BandwidthDispatcherFacade {
    TorrentSeedStats getSeedStatForTorrent(InfoHash infoHash);
}
