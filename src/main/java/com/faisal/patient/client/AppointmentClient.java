package com.faisal.patient.client;

import com.faisal.patient.dto.AppointmentDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AppointmentClient {

    private final RestClient restClient;

    public AppointmentClient(RestClient appointmentRestClient) {
        this.restClient = appointmentRestClient;
    }

    public List<AppointmentDTO> getAppointmentsForPatient(String patientId) {
        AppointmentDTO[] response = restClient.get()
                .uri("/appointments/patients/{patientId}", patientId)
                .retrieve()
                .body(AppointmentDTO[].class);

        return response == null ? List.of() : List.of(response);
    }
}
