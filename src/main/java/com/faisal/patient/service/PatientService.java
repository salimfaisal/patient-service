package com.faisal.patient.service;

import com.faisal.patient.controller.ModelConverter;
import com.faisal.patient.dto.NewPatientDTO;
import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.dto.UpdatePatientDTO;
import com.faisal.patient.entity.PatientEntity;
import com.faisal.patient.exception.InvalidRequestException;
import com.faisal.patient.exception.PatientNotFoundException;
import com.faisal.patient.repository.IPatientRepository;
import com.faisal.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public Collection<PatientDTO> getPatients() {
        return patientRepository.fetchAll()
                .stream().map(ModelConverter::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatient(String patientId) {
        if (!StringUtils.hasText(patientId)) throw new InvalidRequestException();
        Optional<PatientEntity> pOpt = patientRepository.fetchOne(patientId);
        return pOpt.map(ModelConverter::from).orElseThrow(()-> new PatientNotFoundException(patientId));
    }

    @Transactional
    public PatientDTO createPatient(NewPatientDTO newPatientDTO) {
        PatientEntity patientEntity = ModelConverter.toPatientEntity(newPatientDTO);
        PatientEntity savedEntity = patientRepository.insert(patientEntity);
        return ModelConverter.from(savedEntity);
    }

    @Transactional
    public PatientDTO replace(String patientId, UpdatePatientDTO updatePatientDTO) {
        PatientEntity patientEntity = ModelConverter.toPatientEntity(updatePatientDTO);
        Optional<PatientEntity> pOpt = patientRepository.update(patientId, patientEntity);
        return pOpt.map(ModelConverter::from).orElseThrow(()-> new PatientNotFoundException(patientId));
    }
}
