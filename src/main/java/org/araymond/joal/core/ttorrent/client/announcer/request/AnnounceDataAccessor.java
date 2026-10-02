package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.RequiredArgsConstructor;
import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.ConnectionHandler;

import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class AnnounceDataAccessor {

    private final BitTorrentClient bitTorrentClient;
    private final BandwidthDispatcher bandwidthDispatcher;
    private final ConnectionHandler connectionHandler;

    public String getHttpRequestQueryForTorrent(final InfoHash infoHash, final RequestEvent event) {
        return this.bitTorrentClient.createRequestQuery(event, infoHash, this.bandwidthDispatcher.getSeedStatForTorrent(infoHash), this.connectionHandler);
    }

    public Set<Map.Entry<String, String>> getHttpHeadersForTorrent() {
        return this.bitTorrentClient.getHeaders();
    }

    public long getUploaded(final InfoHash infoHash) {
        return this.bandwidthDispatcher.getSeedStatForTorrent(infoHash).getUploaded();
    }

    public long getSpeedInBytesPerSecond(final InfoHash infoHash) {
        return ofNullable(this.bandwidthDispatcher.getSpeedMap().get(infoHash))
                .map(Speed::getBytesPerSecond)
                .orElse(0L);
    }
}
