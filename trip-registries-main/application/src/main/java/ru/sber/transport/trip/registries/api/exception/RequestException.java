package ru.sber.transport.trip.registries.api.exception;

public class RequestException extends RuntimeException {
    
    public RequestException(String message) {
        super(message);
    }
    
    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
