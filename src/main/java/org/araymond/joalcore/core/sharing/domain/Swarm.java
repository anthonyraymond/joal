package org.araymond.joalcore.core.sharing.domain;

import org.araymond.joalcore.annotations.ddd.ValueObject;
import org.araymond.joalcore.core.sharing.domain.services.PeerElection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ValueObject
public final class Swarm {
    public static final Swarm EMPTY = new Swarm(Map.of());

    private final Map<TrackerUniqueIdentifier, Peers> peersByTracker;

    private Swarm(Map<TrackerUniqueIdentifier, Peers> peersByTracker) {
        this.peersByTracker = peersByTracker;
    }

    public Swarm with(TrackerUniqueIdentifier tuid, Peers peers) {
        var map = new HashMap<>(peersByTracker);
        map.put(tuid, peers);

        return new Swarm(map);
    }

    public Optional<Peers> representativePeers(PeerElection election) {
        var elected = this.peersByTracker.values().stream().findFirst();
        if (elected.isEmpty()) {
            return Optional.empty();
        }

        for (Peers candidate : this.peersByTracker.values()) {
            elected = elected.map(self -> election.elect(self, candidate));
        }

        return elected;
    }

    public record TrackerUniqueIdentifier(String value) {
        public TrackerUniqueIdentifier {
            Objects.requireNonNull(value, "TrackerUniqueIdentifier require a non-null [value]");
        }
    }
}
