package com.faisal.patient.service;

import com.faisal.patient.client.AppointmentClient;
import com.faisal.patient.dto.AppointmentDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentQueryService {

    private final AppointmentClient appointmentClient;

    public AppointmentQueryService(AppointmentClient appointmentClient) {
        this.appointmentClient = appointmentClient;
    }

    public List<AppointmentDTO> getAppointmentsForPatient(String patientId) {
        return appointmentClient.getAppointmentsForPatient(patientId);
    }
}
