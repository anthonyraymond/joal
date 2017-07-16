package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
public class AlwaysRefreshPeerIdGenerator extends PeerIdGenerator {

    @JsonCreator
    AlwaysRefreshPeerIdGenerator(
            @JsonProperty(value = "prefix", required = true) final String prefix,
            @JsonProperty(value = "type", required = true) final StringTypes type,
            @JsonProperty(value = "upperCase", required = true) final boolean upperCase,
            @JsonProperty(value = "lowerCase", required = true) final boolean lowerCase
    ) {
        super(prefix, type, upperCase, lowerCase);
    }

    @Override
    public String getPeerId(final MockedTorrent torrent, final RequestEvent event) {
        return super.generatePeerId();
    }

}
