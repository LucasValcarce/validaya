package com.validaya.validaya.exception;

/**
 * Custom exception for general operation failures in the application.
 * Used for business logic errors and validation failures.
 */
public class OperationException extends RuntimeException {
    
    public OperationException(String message) {
        super(message);
    }
    
    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
