package com.faisal.patient.controller;

import com.faisal.patient.dto.AppointmentDTO;
import com.faisal.patient.dto.NewPatientDTO;
import com.faisal.patient.dto.PatientDTO;
import com.faisal.patient.dto.UpdatePatientDTO;
import com.faisal.patient.service.AppointmentQueryService;
import com.faisal.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentQueryService appointmentQueryService;


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
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody NewPatientDTO newPatientDTO) {
        PatientDTO patientDTO = patientService.createPatient(newPatientDTO);
        return ResponseEntity.created(URI.create("/patients/"+patientDTO.id())).body(patientDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> replacePatient(
            @PathVariable String id,
            @Valid @RequestBody UpdatePatientDTO request) {
        PatientDTO patientDTO =  patientService.replace(id, request);
        return ResponseEntity.created(URI.create("/patients/"+id)).body(patientDTO);
    }


    @GetMapping("/{patientId}/appointments")
    public List<AppointmentDTO> getAppointments(
            @PathVariable String patientId) {
        return appointmentQueryService.getAppointmentsForPatient(patientId);
    }

}
