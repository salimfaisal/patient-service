package com.faisal.patient.service;

import com.faisal.patient.controller.ModelConverter;
import com.faisal.patient.dto.NewPatientDTO;
import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.dto.UpdatePatientDTO;
import com.faisal.patient.entity.PatientEntity;
import com.faisal.patient.exception.InvalidRequestException;
import com.faisal.patient.exception.PatientNotFoundException;
import com.faisal.patient.outbox.OutboxEventEntity;
import com.faisal.patient.outbox.OutboxEventJPARepository;
import com.faisal.patient.outbox.PatientCreatedPayload;
import com.faisal.patient.repository.PatientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    //private final PatientEventPublisher patientEventPublisher;
    private final OutboxEventJPARepository outboxEventRepository;
    private final ObjectMapper outboxObjectMapper;

    @Transactional(readOnly = true)
    public Collection<PatientDTO> getPatients() {
        return patientRepository.fetchAll()
                .stream().map(ModelConverter::from)
                .toList();
    }

    @Cacheable(cacheNames = "patients", key = "#patientId")
    @Transactional(readOnly = true)
    public PatientDTO getPatient(String patientId) {
        if (!StringUtils.hasText(patientId)) throw new InvalidRequestException();
        Optional<PatientEntity> pOpt = patientRepository.fetchOne(patientId);
        return pOpt.map(ModelConverter::from).orElseThrow(()-> new PatientNotFoundException(patientId));
    }

    @CachePut(cacheNames = "patients", key = "#result.id()")
    @Transactional
    public PatientDTO createPatient(NewPatientDTO newPatientDTO) {
        PatientEntity patientEntity = ModelConverter.toPatientEntity(newPatientDTO);
        PatientEntity savedEntity = patientRepository.insert(patientEntity);
        //
        // PatientCreatedEvent patientCreatedEvent = new PatientCreatedEvent(savedEntity.getId(), savedEntity.getEmail(), Instant.now());
        // patientEventPublisher.publish(patientCreatedEvent);
        //
        // Outbox
        String payload;
        try {
            PatientCreatedPayload patientCreatedPayload = PatientCreatedPayload.from(savedEntity);
            payload = outboxObjectMapper.writeValueAsString(patientCreatedPayload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create outbox payload", exception);
        }
        outboxEventRepository.save(OutboxEventEntity.pending("Patient",
                savedEntity.getId(), "PatientCreated", payload));

        return ModelConverter.from(savedEntity);
    }

    @CachePut(cacheNames = "patients", key = "#patientId")
    @Transactional
    public PatientDTO replace(String patientId, UpdatePatientDTO updatePatientDTO) {
        PatientEntity patientEntity = ModelConverter.toPatientEntity(updatePatientDTO);
        Optional<PatientEntity> pOpt = patientRepository.update(patientId, patientEntity);
        return pOpt.map(ModelConverter::from).orElseThrow(()-> new PatientNotFoundException(patientId));
    }
}
