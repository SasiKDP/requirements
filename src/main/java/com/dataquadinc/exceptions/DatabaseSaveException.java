package com.dataquadinc.exceptions;

public class DatabaseSaveException extends RuntimeException {
    public DatabaseSaveException(String message) {
        super(message);
    }
}