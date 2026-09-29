package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Объект обмена данными о группе исполнителей.
 */
@Getter
@Setter
@ToString
@Schema(title = "Информация о группе исполнителей", description = "Данные группы исполнителей")
public class ExecutorGroupDTO {

    /**
     * Идентификатор Группы исполнителей.
     */
    @Schema(description = "Идентификатор Группы исполнителей")
    private UUID id;

    /**
     * Услуга.
     */
    @Schema(description = "Услуга")
    private String service;

    /**
     * Человеко-читаемый идентификатор.
     */
    @Schema(description = "Человеко-читаемый идентификатор")
    private String humanReadableId;

    /**
     * Разрешение на редактирование.
     */
    @Schema(description = "Разрешение на редактирование")
    private Boolean editable;

    /**
     * Активность группы исполнителей.
     */
    @Schema(description = "Активность группы исполнителей")
    private Boolean active;

    /**
     * Название группы.
     */
    @Schema(description = "Название группы исполнителей")
    private String name;

    /**
     * Уровень сервиса.
     */
    @Schema(description = "Уровень сервиса")
    private String serviceLevel;

    /**
     * Организация исполнителей.
     */
    @Schema(description = "Организация группы исполнителей")
    private String organizationName;

    /**
     * Идентификатор организации исполнителей.
     */
    @Schema(description = "Идентификатор организации исполнителей")
    private UUID organizationId;

    /**
     * Дата и время создания в UTC.
     */
    @Schema(description = "Дата и время создания в UTC")
    private LocalDateTime creationTime;

    /**
     * Дата и время изменения в UTC.
     */
    @Schema(description = "Дата и время изменения в UTC")
    private LocalDateTime updatedAt;

    /**
     * Идентификатор инициатора.
     */
    @Schema(description = "Идентификатор инициатора")
    private UUID authorId;

    /**
     * Идентификатор последнего модификатора.
     */
    @Schema(description = "Идентификатор последнего модификатора")
    private UUID userId;

    /**
     * Массив исполнителей.
     */
    @Schema(description = "Исполнители")
    private List<ExecutorDTO> executors;

    /**
     * Массив организаций заказчика.
     */
    @Schema(description = "Организации")
    private List<OrganizationExecutorGroupDTO> organizations;

    /**
     * Массив подразделений заказчика.
     */
    @Schema(description = "Подразделения")
    private List<DepartmentExecutorGroupDTO> departments;

    /**
     * Массив заказчиков.
     */
    @Schema(description = "Заказчики")
    private List<EmployeeExecutorGroupDTO> customers;

    /**
     * Массив территорий заказчика.
     */
    @Schema(description = "Территорий заказчика")
    private List<GeoZoneShortDTO> geoZones;

    /**
     * Массив контрагентов в формате UUID.
     */
    @Schema(description = "Контрагенты")
    private List<UUID> contractors;

    /**
     * Дополнительный признак
     */
    @Schema(description = "Дополнительный признак")
    private String additionalFeature;

}
