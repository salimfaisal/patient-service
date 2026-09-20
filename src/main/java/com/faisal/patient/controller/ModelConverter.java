package com.faisal.patient.controller;

import com.faisal.patient.dto.NewPatientDTO;
import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.dto.UpdatePatientDTO;
import com.faisal.patient.entity.PatientEntity;

import java.util.UUID;

public class ModelConverter {

    public static PatientEntity toPatientEntity(NewPatientDTO patientDTO) {
        return new PatientEntity(UUID.randomUUID().toString(), patientDTO.firstName(), patientDTO.lastName(),
                patientDTO.dateOfBirth(), patientDTO.email());
    }

    public static PatientEntity toPatientEntity(UpdatePatientDTO patientDTO) {
        return new PatientEntity(UUID.randomUUID().toString(), patientDTO.firstName(), patientDTO.lastName(),
                patientDTO.dateOfBirth(), patientDTO.email());
    }

    public static PatientDTO from(PatientEntity patientEntity) {
        return new PatientDTO(patientEntity.getId(), patientEntity.getFirstName(),
                patientEntity.getLastName(), patientEntity.getDOB(), patientEntity.getEmail());
    }

}
