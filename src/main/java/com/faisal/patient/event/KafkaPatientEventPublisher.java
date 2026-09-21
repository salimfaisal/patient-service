package com.faisal.patient.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class KafkaPatientEventPublisher  {

    private static final String PATIENT_CREATED_TOPIC = "patient.created";

    private final KafkaTemplate<String, PatientCreatedEvent> kafkaTemplate;

    public void publish(PatientCreatedEvent patientCreatedEvent) {
        kafkaTemplate.send(PATIENT_CREATED_TOPIC, patientCreatedEvent.patientId(), patientCreatedEvent);
    }
}
