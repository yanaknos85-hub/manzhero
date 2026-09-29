package ru.sber.transport.integrations.exception;

public class JwtValidationException extends RuntimeException {
    
    public JwtValidationException(String message) {
        super(message);
    }
    
}
