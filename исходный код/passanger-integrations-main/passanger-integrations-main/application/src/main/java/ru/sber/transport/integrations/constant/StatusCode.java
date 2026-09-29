package ru.sber.transport.integrations.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * код статуса
 */
@Getter
@RequiredArgsConstructor
public enum StatusCode {
    NEW("NEW"),
    REJECT("REJECT"),
    IN_PROGRESS("IN_PROGRESS");
    
    private final String value;
}

