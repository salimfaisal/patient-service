package com.faisal.patient.repository;

import com.faisal.patient.entity.PatientEntity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//@Repository
//@Primary
public class InMemoryIPatientRepository implements IPatientRepository {


    //
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
        return Optional.ofNullable(patientDB.get(patientId));
    }

    public void insert(PatientEntity patientEntity) {
        patientDB.put(patientEntity.getId(), patientEntity);
    }

    public Optional<PatientEntity>  update(String patientId, PatientEntity patientEntity) {
        Optional<PatientEntity> record = fetchOne(patientId);
        if (record.isPresent()) {
            PatientEntity patient = record.get();
            patient.setFirstName(patientEntity.getFirstName());
            patient.setLastName(patientEntity.getLastName());
            patient.setDateOfBirth(patientEntity.getDateOfBirth());
            patient.setEmail(patientEntity.getEmail());
        }
        return record;
    }
}
