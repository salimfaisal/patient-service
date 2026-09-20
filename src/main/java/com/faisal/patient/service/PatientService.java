package com.faisal.patient.service;

import com.faisal.patient.controller.ModelConverter;
import com.faisal.patient.dto.NewPatientDTO;
import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.dto.UpdatePatientDTO;
import com.faisal.patient.entity.PatientEntity;
import com.faisal.patient.exception.InvalidRequestException;
import com.faisal.patient.exception.PatientNotFoundException;
import com.faisal.patient.repository.PatientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.swing.text.html.Option;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public Collection<PatientDTO> getPatients() {
        return patientRepository.fetchAll()
                .stream().map(ModelConverter::from)
                .toList();
    }

    public PatientDTO getPatient(String patientId) {
        if (!StringUtils.hasText(patientId)) throw new InvalidRequestException();
        Optional<PatientEntity> pOpt = patientRepository.fetchOne(patientId);
        return pOpt.map(ModelConverter::from).orElseThrow(()-> new PatientNotFoundException(patientId));
    }

    public PatientDTO createPatient(NewPatientDTO newPatientDTO) {
        PatientEntity patientEntity = ModelConverter.toPatientEntity(newPatientDTO);
        patientRepository.insert(patientEntity);
        return ModelConverter.from(patientEntity);
    }

    public PatientDTO replace(String patientId, UpdatePatientDTO updatePatientDTO) {
        PatientEntity patientEntity = ModelConverter.toPatientEntity(updatePatientDTO);
        Optional<PatientEntity> pOpt = patientRepository.update(patientId, patientEntity);
        return pOpt.map(ModelConverter::from).orElseThrow(()-> new PatientNotFoundException(patientId));
    }
}
