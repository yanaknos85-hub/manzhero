package ru.sber.transport.corporate.business.model;

import lombok.Data;

import java.util.UUID;

/**
 * Атрибуты сотрудника.
 */
@Data
public class Attribute {

    /**
     * Идентификатор.
     */
    private UUID id;

    /**
     * Название.
     */
    private String name;

    /**
     * Статус.
     */
    private Active status;

}
