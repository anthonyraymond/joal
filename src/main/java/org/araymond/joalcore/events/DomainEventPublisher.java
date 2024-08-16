package org.araymond.joalcore.events;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

    default void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
