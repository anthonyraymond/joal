package org.araymond.joalcore.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class DomainEvent {
    private final UUID uuid;
    private final LocalDateTime occurredAt;

    public DomainEvent() {
        uuid = UUID.randomUUID();
        occurredAt = LocalDateTime.now();
    }

    public UUID uuid() {
        return uuid;
    }

    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}
