package org.araymond.joal.web.messages.outgoing.impl.speed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.araymond.joal.core.events.speed.SeedingSpeedsHasChangedEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.util.List;
import java.util.stream.Collectors;

public class SeedingSpeedHasChangedPayload implements MessagePayload {
    private final List<SpeedPayload> speeds;

    public SeedingSpeedHasChangedPayload(final SeedingSpeedsHasChangedEvent event) {
        this.speeds = event.getSpeeds().entrySet().stream()
                .map(entry -> new SpeedPayload(entry.getKey(), entry.getValue().getBytesPerSeconds()))
                .collect(Collectors.toList());
    }

    public List<SpeedPayload> getSpeeds() {
        return speeds;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class SpeedPayload {
        private final InfoHash infoHash;
        private final Long bytesPerSeconds;
    }
}
