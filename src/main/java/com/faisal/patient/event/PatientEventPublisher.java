package com.faisal.patient.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientEventPublisher {

    private final KafkaPatientEventPublisher kafkaPatientEventPublisher;

    public void publish(PatientCreatedEvent patientCreatedEvent) {
        kafkaPatientEventPublisher.publish(patientCreatedEvent);
    }
}
