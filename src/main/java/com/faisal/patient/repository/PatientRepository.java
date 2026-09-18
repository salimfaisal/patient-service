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

@Repository
public class PatientRepository {

    // temp
    private final static Map<String, PatientEntity> patientDB = new ConcurrentHashMap<>();
    static {
        var id = UUID.randomUUID().toString();
        patientDB.put(id, new PatientEntity(id, "Alice", "Wonder",
                LocalDate.of(2000, 1, 1), "alice@gmail.com"));
        id = UUID.randomUUID().toString();
        patientDB.put(id, new PatientEntity(id, "John", "Watt",
                LocalDate.of(1900, 1, 2), "john@gmail.com"));
        id = UUID.randomUUID().toString();
        patientDB.put(id, new PatientEntity(id, "Ben", "Franklin",
                LocalDate.of(1800, 1, 3), "ben@gmail.com"));

    }

    /**
     *
     */
    public Collection<PatientEntity> fetchAll() {
        return patientDB.values();
    }

    /**
     *
     */
    public Optional<PatientEntity> fetchOne(String patientId) {
        if (!StringUtils.hasText(patientId)) throw new InvalidRequestException();
        return Optional.ofNullable(patientDB.get(patientId));
    }

    public void insert(PatientEntity patientEntity) {
        patientDB.put(patientEntity.getId(), patientEntity);
    }
}
