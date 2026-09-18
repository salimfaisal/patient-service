package com.faisal.patient.dto;

import com.faisal.patient.entity.PatientEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PatientDTO(
        @NotBlank String id,
        @NotBlank String firstName,
        String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Email String email
        ) {

        public static PatientDTO from(PatientEntity patientEntity) {
                return new PatientDTO(patientEntity.getId(), patientEntity.getFirstName(),
                        patientEntity.getLastName(), patientEntity.getDOB(), patientEntity.getEmail());
        }

        public static PatientEntity toPatientEntity(PatientDTO patientDTO) {
                return new PatientEntity(patientDTO.id(), patientDTO.firstName(), patientDTO.lastName(),
                        patientDTO.dateOfBirth(), patientDTO.email());
        }
}
