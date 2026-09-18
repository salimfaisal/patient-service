package com.faisal.patient.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PatientEntity {

    @NotBlank
    private String id;
    @NotBlank
    private String firstName;
    private String lastName;
    @NotNull
    private LocalDate dOB;
    @NotBlank
    @Email private String email;


}
