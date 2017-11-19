package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
public class NeverRefreshPeerIdGenerator extends PeerIdGenerator {

    private final String peerId;
    @JsonCreator
    NeverRefreshPeerIdGenerator(

            @JsonProperty(value = "algorithm", required = true) final PeerIdAlgorithm algorithm,
            @JsonProperty(value = "shouldUrlEncode", required = true) final boolean isUrlEncoded
    ) {
        super(algorithm, isUrlEncoded);

        this.peerId = super.generatePeerId();
    }

    @Override
    public String getPeerId(final MockedTorrent torrent, final RequestEvent event) {
        return peerId;
    }

}
