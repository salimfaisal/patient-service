package com.faisal.patient.repository;

import com.faisal.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientJPARepository extends JpaRepository<PatientEntity, String> {

    boolean existsByEmailIgnoreCase(String email);
}
