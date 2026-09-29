package ru.sber.transport.corporate.business.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/// Бизнес-модель контакта
@Data
@EqualsAndHashCode
public class Contact {

    /// Идентификатор контакта
    private UUID id;

    /// Тип контакта
    private ContactType type;

    /// Значение контакта
    private String value;
}
