package com.faisal.patient.client;

import com.faisal.patient.dto.AppointmentDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import com.faisal.patient.exception.AppointmentServiceUnavailableException;

import java.util.List;

@Component
public class AppointmentClient {

    private final RestClient restClient;

    public AppointmentClient(RestClient appointmentRestClient) {
        this.restClient = appointmentRestClient;
    }

    @CircuitBreaker(
        name = "appointmentRead",
        fallbackMethod = "appointmentsFallback"
    )
    @Retry(name = "appointmentRead")
    public List<AppointmentDTO> getAppointmentsForPatient(String patientId) {
        AppointmentDTO[] response = restClient.get()
                .uri("/appointments/patients/{patientId}", patientId)
                .retrieve()
                .body(AppointmentDTO[].class);

        return response == null ? List.of() : List.of(response);
    }


    @SuppressWarnings("unused")
    private List<AppointmentDTO> appointmentsFallback(String patientId, Throwable ex) {
    throw new AppointmentServiceUnavailableException(
        "Appointment service is temporarily unavailable", ex
    );
}
}
