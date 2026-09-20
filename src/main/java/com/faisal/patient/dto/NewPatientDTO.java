package com.faisal.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record NewPatientDTO(
        @NotBlank String firstName,
        String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Email String email
        ) {
}
