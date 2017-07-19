package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.type.PeerIdTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshPeerIdGenerator extends PeerIdGenerator {
    private final Map<MockedTorrent, String> keyPerTorrent;

    @JsonCreator
    TorrentVolatileRefreshPeerIdGenerator(
            @JsonProperty(value = "prefix", required = true) final String prefix,
            @JsonProperty(value = "type", required = true) final PeerIdTypes type,
            @JsonProperty(value = "upperCase", required = true) final boolean upperCase,
            @JsonProperty(value = "lowerCase", required = true) final boolean lowerCase
    ) {
        super(prefix, type, upperCase, lowerCase);
        keyPerTorrent = new HashMap<>();
    }

    @Override
    public String getPeerId(final MockedTorrent torrent, final RequestEvent event) {
        final String peerId;

        if (!this.keyPerTorrent.containsKey(torrent)) {
            this.keyPerTorrent.put(torrent, super.generatePeerId());
        }

        peerId = this.keyPerTorrent.get(torrent);

        if (event == RequestEvent.STOPPED) {
            this.keyPerTorrent.remove(torrent);
        }

        return peerId;
    }
}
