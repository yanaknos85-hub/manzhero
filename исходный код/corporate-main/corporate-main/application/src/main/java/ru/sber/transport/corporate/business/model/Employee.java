package ru.sber.transport.corporate.business.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Класс сотрудника.
 */
@Data
public class Employee implements HasOrganizationStructure {

    /**
     * Идентификатор сотрудника.
     */
    private UUID id;

    /**
     * Табельный номер сотрудника.
     */
    private String personnelNumber;

    /**
     * Статус сотрудника.
     */
    private Active status;

    /**
     * Подтверждение подписания ПДн.
     */
    private boolean consent;

    /**
     * МВЗ.
     */
    private String costCenter;

    /**
     * Email.
     */
    private String email;

    /**
     * Внешний email.
     */
    private String externalEmail;

    /**
     * Дата увольнения.
     */
    private LocalDate fireDate;

    /**
     * Пол.
     */
    private Gender gender;

    /**
     * Имя.
     */
    private String firstName;

    /**
     * Характер работы.
     */
    private ItinerantType itinerant;

    /**
     * Фамилия.
     */
    private String lastName;

    /**
     * Серия свидетельства о браке.
     */
    private String marriageCertificate;

    /**
     * Отчество.
     */
    private String patronymic;

    /**
     * Номер комнаты.
     */
    private String room;

    /**
     * Идентификатор должности.
     */
    private UUID positionId;

    /**
     * Идентификатор подразделения.
     */
    private UUID departmentId;

    /**
     * Идентификатор организации.
     */
    private UUID organizationId;

    /**
     * Идентификатор руководителя.
     */
    private UUID supervisorId;

    /**
     * Человекочитаемый идентификатор.
     */
    private String humanReadableId;

    /**
     * Номер телефона.
     */
    private String phone;

    /**
     * Тип пользователя.
     */
    private StructureType type;

    /**
     * Признак подтверждения номера телефона.
     */
    private boolean phoneConfirmed;

    /**
     * Наименование должности.
     */
    // TODO уточнить актуальность.
    private String positionName;

    /**
     * Список подчиненных подразделений.
     */
    // TODO уточнить актуальность.
    private List<UUID> managedDepartments;

    /**
     * Признак того, что сотрудник является руководителем.
     */
    // TODO уточнить актуальность.
    private boolean departmentHead;

    /**
     * Атрибуты сотрудника.
     */
    // TODO уточнить актуальность.
    private List<Attribute> attributes;

    /**
     * Количество согласований.
     */
    // TODO уточнить актуальность.
    private int approvals;

    @Override
    public StructureType getStructureType() {
        return type;
    }

    @Override
    public void setStructureType(StructureType structureType) {
        type = structureType;
    }

    @Override
    public String getSyncId() {
        return personnelNumber;
    }

    @Override
    public void setSyncId(String syncId) {
        personnelNumber = syncId;
    }
}
