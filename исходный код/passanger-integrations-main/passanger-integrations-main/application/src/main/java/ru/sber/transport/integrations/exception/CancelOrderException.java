package ru.sber.transport.integrations.exception;

public class CancelOrderException extends RuntimeException {

    public CancelOrderException(String message) {
        super(message);
    }
    
}
