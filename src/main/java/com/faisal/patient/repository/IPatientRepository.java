package com.faisal.patient.repository;

import com.faisal.patient.entity.PatientEntity;
import java.util.*;

public interface IPatientRepository {

    public Collection<PatientEntity> fetchAll();

    public Optional<PatientEntity> fetchOne(String patientId);

    public void insert(PatientEntity patientEntity);

    public Optional<PatientEntity> update(String patientId, PatientEntity patientEntity);
}
