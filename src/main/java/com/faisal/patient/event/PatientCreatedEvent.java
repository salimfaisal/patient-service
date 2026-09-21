package com.faisal.patient.event;

import java.time.Instant;

public record PatientCreatedEvent(
   String patientId,
   String email,
   Instant eventAt

) {
}
