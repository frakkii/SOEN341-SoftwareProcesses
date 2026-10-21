package com.example.demo.exception;

import org.springframework.http.HttpStatus;

// A job application request that can't be completed. The status is the HTTP
// status sent back (400 Bad Request unless the caller picks another one).
public class ApplicationException extends RuntimeException {

    private final HttpStatus status;

    public ApplicationException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public ApplicationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
