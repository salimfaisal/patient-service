package com.faisal.patient.controller;

import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping({"","/"})
    public Collection<PatientDTO> listPatients(
    ) {
        return patientService.getPatients();
    }

    @GetMapping("/{id}")
    public PatientDTO getPatient(@PathVariable String id) {
        return patientService.getPatient(id);
    }

    @PostMapping({"","/"})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<@Valid PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        patientService.createPatient(patientDTO);
        return ResponseEntity.created(URI.create("/patients/"+patientDTO.id())).body(patientDTO);
    }
}
