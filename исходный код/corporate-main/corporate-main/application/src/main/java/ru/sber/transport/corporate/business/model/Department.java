package ru.sber.transport.corporate.business.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Класс подразделения.
 */
@Data
public class Department implements HasOrganizationStructure {

    /**
     * Идентификатор подразделения.
     */
    private UUID id;

    /**
     * Код подразделения.
     */
    @NonNull
    private String code;

    /**
     * Название подразделения.
     */
    @NonNull
    private String name;

    /**
     * Название уровня подразделения.
     */
    private String levelName;

    /**
     * Код уровня подразделения.
     */
    private String levelCode;

    /**
     * Тип подразделения.
     */
    private String fosType;

    /**
     * Дата начала действия подразделения.
     */
    private LocalDate startDate;

    /**
     * Дата окончания действия подразделения.
     */
    private LocalDate endDate;

    /**
     * Статус подразделения.
     */
    @NonNull
    private Active status;

    /**
     * Идентификатор организации
     */
    private UUID organizationId;

    /**
     * Идентификатор родительского подразделения
     */
    private UUID parentId;

    /**
     * Идентификатор руководителя подразделения
     */
    private UUID headId;

    /**
     * Человекочитаемый идентификатор
     */
    private String humanReadableId;

    /**
     * Идентификатор синхронизации
     */
    private String syncId;

    /**
     * Местоположение
     */
    private String location;

    /**
     * Тип структуры
     */
    @NonNull
    private StructureType structureType;

    /**
     * Признак того, что подразделение сделано вручную
     */
    private boolean handmade;

    /**
     * Дата обновления.
     */
    private OffsetDateTime updateDate;
}
