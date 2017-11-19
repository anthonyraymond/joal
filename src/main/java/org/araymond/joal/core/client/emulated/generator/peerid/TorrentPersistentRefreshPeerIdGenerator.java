package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentPersistentRefreshPeerIdGenerator extends PeerIdGenerator {
    private final Map<MockedTorrent, AccessAwarePeerId> peerIdPerTorrent;

    private int getCounter = 0;

    @JsonCreator
    TorrentPersistentRefreshPeerIdGenerator(
            @JsonProperty(value = "algorithm", required = true) final PeerIdAlgorithm algorithm,
            @JsonProperty(value = "shouldUrlEncode", required = true) final boolean isUrlEncoded
    ) {
        super(algorithm, isUrlEncoded);
        peerIdPerTorrent = new HashMap<>();
    }

    @Override
    public String getPeerId(final MockedTorrent torrent, final RequestEvent event) {
        if (!this.peerIdPerTorrent.containsKey(torrent)) {
            this.peerIdPerTorrent.put(torrent, new AccessAwarePeerId(super.generatePeerId()));
        }

        final String key = this.peerIdPerTorrent.get(torrent).getPeerId();
        getCounter++;
        if (getCounter >= 30) {
            getCounter = 0;
            evictOldEntries();
        }
        return key;
    }

    private void evictOldEntries() {
        Sets.newHashSet(this.peerIdPerTorrent.entrySet()).stream()
                .filter(this::shouldEvictEntry)
                .forEach(entry -> this.peerIdPerTorrent.remove(entry.getKey()));
    }

    /**
     * If an entry is older than one hour and a half, it shoul be considered as evictable
     *
     * @param entry decide whether this entry is evictable
     * @return true if evictable, false otherwise
     */
    @VisibleForTesting
    boolean shouldEvictEntry(final Map.Entry<MockedTorrent, AccessAwarePeerId> entry) {
        return ChronoUnit.MINUTES.between(entry.getValue().getLastAccess(), LocalDateTime.now()) >= 120;
    }

    static class AccessAwarePeerId {
        private final String peerId;
        private LocalDateTime lastAccess;

        @VisibleForTesting
        AccessAwarePeerId(final String peerId) {
            this.peerId = peerId;
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
