package com.dataquadinc.exceptions;

public class NoJobsFoundException extends RuntimeException {
    public NoJobsFoundException(String message) {
        super(message);
    }
}