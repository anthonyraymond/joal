package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.bandwith.BandwidthDispatcherFacade;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;

import java.util.List;
import java.util.Map;

public class AnnounceDataAccessor {

    private final BitTorrentClient bitTorrentClient;
    private final BandwidthDispatcherFacade bandwidthDispatcher;
    private final ConnectionHandler connectionHandler;

    public AnnounceDataAccessor(final BitTorrentClient bitTorrentClient, final BandwidthDispatcherFacade bandwidthDispatcher, final ConnectionHandler connectionHandler) {
        this.bitTorrentClient = bitTorrentClient;
        this.bandwidthDispatcher = bandwidthDispatcher;
        this.connectionHandler = connectionHandler;
    }

    public String getHttpRequestQueryForTorrent(final InfoHash infoHash, final RequestEvent event) {
        return this.bitTorrentClient.createRequestQuery(event, infoHash, this.bandwidthDispatcher.getSeedStatForTorrent(infoHash), this.connectionHandler);
    }

    public List<Map.Entry<String, String>> getHttpHeadersForTorrent() {
        return this.bitTorrentClient.createRequestHeaders();
    }

}
