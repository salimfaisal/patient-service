package com.faisal.patient.dto;

import com.faisal.patient.entity.PatientEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PatientDTO(
        @NotBlank String id,
        @NotBlank String firstName,
        String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Email String email
        ) {
}
