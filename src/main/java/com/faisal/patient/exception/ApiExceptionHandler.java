package com.faisal.patient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleInvalidRequest(InvalidRequestException invalidRequestException) {
        return new ErrorResponse(invalidRequestException.getMessage());
    }

    public record ErrorResponse(String message) {}
}
