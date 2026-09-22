package com.dextime.api.exception;

public class ProcedureNotFoundException extends RuntimeException {
    public ProcedureNotFoundException(String message) {
        super(message);
    }
}
