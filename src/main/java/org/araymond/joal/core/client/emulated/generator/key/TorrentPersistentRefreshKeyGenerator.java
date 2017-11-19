package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentPersistentRefreshKeyGenerator extends KeyGenerator {
    private final Map<MockedTorrent, AccessAwareKey> keyPerTorrent;

    @JsonCreator
    TorrentPersistentRefreshKeyGenerator(
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
        keyPerTorrent = new HashMap<>();
    }

    @Override
    public String getKey(final MockedTorrent torrent, final RequestEvent event) {
        if (!this.keyPerTorrent.containsKey(torrent)) {
            this.keyPerTorrent.put(torrent, new AccessAwareKey(super.generateKey()));
        }

        final String key = this.keyPerTorrent.get(torrent).getPeerId();
        evictOldEntries();
        return key;
    }

    private void evictOldEntries() {
        Sets.newHashSet(this.keyPerTorrent.entrySet()).stream()
                .filter(this::shouldEvictEntry)
                .forEach(entry -> this.keyPerTorrent.remove(entry.getKey()));
    }

    /**
     * If an entry is older than two hours, it should be considered as evictable
     *
     * @param entry decide whether this entry is evictable
     * @return true if evictable, false otherwise
     */
    @VisibleForTesting
    boolean shouldEvictEntry(final Map.Entry<MockedTorrent, AccessAwareKey> entry) {
        return ChronoUnit.MINUTES.between(entry.getValue().getLastAccess(), LocalDateTime.now()) >= 120;
    }

    static class AccessAwareKey {
        private final String peerId;
        private LocalDateTime lastAccess;

        @VisibleForTesting
        AccessAwareKey(final String key) {
            this.peerId = key;
            this.lastAccess = LocalDateTime.now();
        }

        public String getPeerId() {
            this.lastAccess = LocalDateTime.now();
            return this.peerId;
        }

        LocalDateTime getLastAccess() {
            return lastAccess;
        }
    }
}
