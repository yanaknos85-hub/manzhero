package ru.sber.transport.corporate.business.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Бизнес-модель организации
 */
@Data
@EqualsAndHashCode
public class Organization {

    /**
     * Идентификатор синхронизации.
     */
    private String syncId;

    /**
     * Название организации.
     */
    private String name;

    /**
     * Адрес организации.
     */
    private String address;

    /**
     * Контакты организации.
     */
    private List<Contact> contacts = new ArrayList<>();

    /**
     * ОГРН.
     */
    private String msrn;

    /**
     * Код организации.
     */
    private Integer code;

    /**
     * ИНН.
     */
    private String tid;

    // ----------------

    /**
     * Идентификатор организации.
     */
    private UUID id;

    /**
     * Цифровой идентификатор организации.
     */
    private long digitId;

    /**
     * Статус активности.
     */
    private Active status;

    /**
     * Группа организаций.
     */
    private UUID groupId;

    /**
     * Список доступных классов транспорта.
     */
    private List<String> availableClasses;
}
