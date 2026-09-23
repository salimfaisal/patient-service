package com.faisal.patient.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor
public class OutboxEventEntity {

    @Id
    private String id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status;

    private int retryCount;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private Instant createdAt;
    private Instant publishedAt;

    public static OutboxEventEntity pending(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload) {

        OutboxEventEntity event = new OutboxEventEntity();
        event.id = UUID.randomUUID().toString();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.payload = payload;
        event.status = OutboxEventStatus.PENDING;
        event.createdAt = Instant.now();
        return event;
    }

    public void markPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markFailed(Exception exception) {
        this.retryCount++;
        this.lastError = exception.getMessage();
    }
}