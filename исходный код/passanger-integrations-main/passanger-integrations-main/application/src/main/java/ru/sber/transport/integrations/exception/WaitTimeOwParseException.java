package ru.sber.transport.integrations.exception;

public class WaitTimeOwParseException extends RuntimeException {

    public WaitTimeOwParseException(String value) {
        super("Cant parse waitTimeOw, value:" + value);
    }
}