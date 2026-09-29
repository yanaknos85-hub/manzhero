package ru.sber.transport.integrations.exception;

public class ServerErrorException extends RuntimeException {
    
    public ServerErrorException(String message) {
        super(message);
    }
    
}
