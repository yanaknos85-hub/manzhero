package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение с контактными данными.
 */
@Jacksonized
@Builder
@Getter
public class ContactMessage implements Message<UUID> {

    /**
     * Идентификатор.
     */
    private final UUID id;

    /**
     * Тип.
     */
    private final String type;


    /**
     * Значение.
     */
    private final String value;

    /**
     * Использование по-умолчанию.
     */
    private static final String using = "DEFAULT";

    /**
     * Идентификатор сотрудника.
     */
    private final UUID employeeId;

    /**
     * флаг - удален.
     */
    @Builder.Default
    private final boolean deleted = false;
    
}
