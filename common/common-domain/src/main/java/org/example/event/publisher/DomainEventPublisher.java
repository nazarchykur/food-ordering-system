package org.example.event.publisher;

import org.example.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {
    void publish(T domainEvent);
}
