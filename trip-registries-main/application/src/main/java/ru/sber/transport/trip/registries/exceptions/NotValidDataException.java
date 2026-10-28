package ru.sber.transport.trip.registries.exceptions;

/**
 * Исключение невалидных данных.
 */
public class NotValidDataException extends Exception {
    
    /**
     * Создать новое исключение.
     *
     * @param message сообщение.
     * @param cause причина.
     */
    public NotValidDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
