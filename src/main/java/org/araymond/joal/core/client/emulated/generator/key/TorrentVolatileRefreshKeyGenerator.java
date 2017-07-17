package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshKeyGenerator extends KeyGenerator {
    private final Map<MockedTorrent, String> keyPerTorrent;

    @JsonCreator
    TorrentVolatileRefreshKeyGenerator(
            @JsonProperty(value = "length", required = true) final Integer length,
            @JsonProperty(value = "type", required = true) final StringTypes type,
            @JsonProperty(value = "upperCase", required = true) final boolean upperCase,
            @JsonProperty(value = "lowerCase", required = true) final boolean lowerCase
    ) {
        super(length, type, upperCase, lowerCase);
        keyPerTorrent = new HashMap<>();
    }

    @Override
    public String getKey(final MockedTorrent torrent, final RequestEvent event) {
        final String key;

        if (!this.keyPerTorrent.containsKey(torrent)) {
            this.keyPerTorrent.put(torrent, super.generateKey());
        }

        key = this.keyPerTorrent.get(torrent);

        if (event == RequestEvent.STOPPED) {
            this.keyPerTorrent.remove(torrent);
        }

        return key;
    }
}
