package com.faisal.patient.repository;

import com.faisal.patient.entity.PatientEntity;
import com.faisal.patient.exception.DuplicateEmailException;
import com.faisal.patient.exception.PatientNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PatientRepository {

    private final PatientJPARepository patientJPARepository;

    public Collection<PatientEntity> fetchAll() {
        return patientJPARepository.findAll();
    }

    public Optional<PatientEntity> fetchOne(String patientId) {
        return patientJPARepository.findById(patientId);
    }

    public PatientEntity insert(PatientEntity patientEntity) {
        if (patientJPARepository.existsByEmailIgnoreCase(patientEntity.getEmail())) {
            throw new DuplicateEmailException(patientEntity.getEmail());
        }
        PatientEntity patient = new PatientEntity();
        patient.setFirstName(patientEntity.getFirstName());
        patient.setLastName(patientEntity.getLastName());
        patient.setEmail(patientEntity.getEmail());
        patient.setDateOfBirth(patientEntity.getDateOfBirth());

        return patientJPARepository.save(patient);
    }

    public Optional<PatientEntity> update(String patientId, PatientEntity patientEntity) {
        PatientEntity patient = patientJPARepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
        patient.setFirstName(patientEntity.getFirstName());
        patient.setLastName(patientEntity.getLastName());
        patient.setEmail(patientEntity.getEmail());
        patient.setDateOfBirth(patientEntity.getDateOfBirth());
        return Optional.of(patientJPARepository.save(patient));
    }
}
