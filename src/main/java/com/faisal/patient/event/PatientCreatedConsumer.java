package com.faisal.patient.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PatientCreatedConsumer {

    private static final String PATIENT_CREATED_TOPIC = "patient.created";

    @KafkaListener (
        topics = PATIENT_CREATED_TOPIC, groupId = "patient-created-audit", concurrency = "1" // 1 consumer in group
    )
    public void onPatientCreated(PatientCreatedEvent event) {
        System.out.println(
                "Patient created: " + event.patientId()
        );
    }
}
