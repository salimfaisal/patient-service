package com.faisal.patient.service;

import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.entity.PatientEntity;
import com.faisal.patient.exception.InvalidRequestException;
import com.faisal.patient.repository.PatientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public Collection<PatientDTO> getPatients() {
        return patientRepository.fetchAll()
                .stream().map(PatientDTO::from)
                .toList();
    }

    public PatientDTO getPatient(String id) {
        Optional<PatientEntity> pOpt = patientRepository.fetchOne(id);
        return pOpt.map(PatientDTO::from).orElseThrow(() -> new InvalidRequestException());
    }

    public void createPatient(@Valid PatientDTO patientDTO) {
        patientRepository.insert(PatientDTO.toPatientEntity(patientDTO));
    }
}
