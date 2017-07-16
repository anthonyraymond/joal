package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
public class NeverRefreshKeyGenerator extends KeyGenerator {

    private final String key;
    @JsonCreator
    NeverRefreshKeyGenerator(
            @JsonProperty(value = "length", required = true) final Integer length,
            @JsonProperty(value = "type", required = true) final StringTypes type,
            @JsonProperty(value = "upperCase", required = true) final boolean upperCase,
            @JsonProperty(value = "lowerCase", required = true) final boolean lowerCase
    ) {
        super(length, type, upperCase, lowerCase);

        this.key = generateKey();
    }

    @Override
    public String getKey(final MockedTorrent torrent, final RequestEvent event) {
        return key;
    }

}
