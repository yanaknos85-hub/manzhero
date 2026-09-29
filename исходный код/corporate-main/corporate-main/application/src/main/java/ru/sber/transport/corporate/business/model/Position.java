package ru.sber.transport.corporate.business.model;

import lombok.Data;
import lombok.NonNull;
import ru.sberbank.ditsib.corpclient.database.model.HasOrganization;

import java.util.Set;
import java.util.UUID;

/**
 * Класс должности
 */
@Data
public class Position implements HasOrganizationStructure {

    /**
     * Идентификатор должности
     */
    private UUID id;

    /**
     * Название должности
     */
    @NonNull
    private String name;

    /**
     * Статус должности
     */
    @NonNull
    private Active status;

    /**
     * Идентификатор организации.
     */
    private UUID organizationId;

    /**
     * Тип структуры.
     */
    @NonNull
    private StructureType structureType;

    /**
     * Человекочитаемый идентификатор.
     */
    private String humanReadableId;

    /**
     * Согласование не требуется.
     */
    private boolean noApproveRequired;

    /**
     * Идентификатор синхронизации.
     */
    private String syncId;

    private Set<String> availableClasses;

}
