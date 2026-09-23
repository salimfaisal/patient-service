package com.faisal.patient.event;

import com.faisal.patient.outbox.PatientCreatedPayload;
import com.faisal.patient.outbox.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientCreatedConsumer {

    private static final String PATIENT_CREATED_TOPIC = "patient.created";

    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper outboxObjectMapper;


    @KafkaListener (
        topics = PATIENT_CREATED_TOPIC, groupId = "patient-created-audit", concurrency = "1" // 1 consumer in group
    )
    @Transactional
    public void onPatientCreated(String payload,
                                 @Header("eventId") String eventId) throws JsonProcessingException {
        System.out.println(
                "Received event: " + eventId
        );
        System.out.println(
                "Patient created: " + payload
        );
        //
        if (processedEventRepository.recordIfNew(eventId) == 0) {
            log.info("Ignoring already processed event {}", eventId);
            return;
        }
        PatientCreatedPayload patient =
                outboxObjectMapper.readValue(payload, PatientCreatedPayload.class);

        // Do database-based consumer work here.
        log.info("Processing patient {}", patient.id());
    }
}
