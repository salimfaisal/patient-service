package com.faisal.patient.exception;

public class InvalidRequestException extends RuntimeException{

    public InvalidRequestException() {
        super("Invalid Request");
    }
}
