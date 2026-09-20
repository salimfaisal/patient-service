package com.faisal.patient.repository;

import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.entity.PatientEntity;
import com.faisal.patient.exception.InvalidRequestException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface PatientRepository {

    public Collection<PatientEntity> fetchAll();

    public Optional<PatientEntity> fetchOne(String patientId);

    public void insert(PatientEntity patientEntity);

    public Optional<PatientEntity> update(String patientId, PatientEntity patientEntity);
}
