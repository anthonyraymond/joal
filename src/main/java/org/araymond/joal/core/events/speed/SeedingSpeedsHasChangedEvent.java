package org.araymond.joal.core.events.speed;

import com.google.common.collect.Maps;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.util.Map;

public class SeedingSpeedsHasChangedEvent {
    private final Map<InfoHash, Speed> speeds;

    public SeedingSpeedsHasChangedEvent(final Map<InfoHash, Speed> speeds) {
        this.speeds = Maps.newHashMap(speeds);
    }

    public Map<InfoHash, Speed> getSpeeds() {
        return speeds;
    }
}
