package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
public class AlwaysRefreshPeerIdGenerator extends PeerIdGenerator {

    @JsonCreator
    AlwaysRefreshPeerIdGenerator(
            @JsonProperty(value = "pattern", required = true) final String pattern,
            @JsonProperty(value = "shouldUrlEncode", required = true) final boolean isUrlEncoded
    ) {
        super(pattern, isUrlEncoded);
    }

    @Override
    public String getPeerId(final MockedTorrent torrent, final RequestEvent event) {
        return super.generatePeerId();
    }

}
