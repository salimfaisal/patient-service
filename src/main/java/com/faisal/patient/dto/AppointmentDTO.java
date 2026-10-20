package com.faisal.patient.dto;

import java.time.Instant;

public record AppointmentDTO(
        String id,
        String patientId,
        String description,
        Instant scheduledAt,
        String status
) {}
