package com.faisal.patient.outbox;

import com.faisal.patient.entity.PatientEntity;

import java.time.LocalDate;

public record PatientCreatedPayload(
        String id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String email
) {
    public static PatientCreatedPayload from(PatientEntity patient) {
        return new PatientCreatedPayload(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getEmail()
        );
    }
}