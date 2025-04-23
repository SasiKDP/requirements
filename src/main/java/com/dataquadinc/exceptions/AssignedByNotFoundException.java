package com.dataquadinc.exceptions;

public class AssignedByNotFoundException extends RuntimeException {
    public AssignedByNotFoundException(String message) {
        super(message);
    }
}