package com.faisal.patient.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventJPARepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var events = outboxEventRepository
                .findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        for (OutboxEventEntity event : events) {
            try {
                var message = MessageBuilder.withPayload(event.getPayload())
                        .setHeader(KafkaHeaders.TOPIC, topicFor(event))
                        .setHeader(KafkaHeaders.KEY, event.getAggregateId())
                        .setHeader("eventId", event.getId())
                        .setHeader("eventType", event.getEventType())
                        .build();
//                kafkaTemplate.send(
//                        topicFor(event),
//                        event.getAggregateId(),
//                        event.getPayload()
//                ).get(); // Wait for Kafka acknowledgement.
                kafkaTemplate.send(message).get();
                event.markPublished();

                log.info("Published outbox event {}", event.getId());
            }
            catch (Exception exception) {
                event.markFailed(exception);

                log.warn(
                        "Could not publish outbox event {}. Attempt {}",
                        event.getId(),
                        event.getRetryCount(),
                        exception);
            }
        }
    }

    private String topicFor(OutboxEventEntity event) {
        return switch (event.getEventType()) {
            case "PatientCreated" -> "patient.created";
            default -> throw new IllegalArgumentException(
                    "No Kafka topic configured for " + event.getEventType());
        };
    }
}