package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@Schema(title = "Новые данные о группе исполнителей", description = "Данные группы исполнителей")
public class NewExecutorGroupDTO {

    /**
     * Человеко-читаемый идентификатор.
     */
    @Schema(description = "Человеко-читаемый идентификатор")
    private String humanReadableId;

    /**
     * Активность группы исполнителей.
     */
    @Schema(description = "Активность группы исполнителей")
    private Boolean active;

    /**
     * Услуга.
     */
    @NotBlank
    @Schema(description = "Услуга")
    private String service;

    /**
     * Название группы.
     */
    @NotBlank
    @Schema(description = "Название группы исполнителей")
    @Pattern(regexp = "^(?=.{1,150}$)[\\sа-яА-Я-]+/[\\sа-яА-Я0-9№-]+/[\\sа-яА-Я]+$")
    private String name;

    /**
     * Уровень сервиса.
     */
    @Schema(description = "Уровень сервиса")
    private String serviceLevel;

    /**
     * Идентификатор организации исполнителей.
     */
    @NotNull
    @Schema(description = "Идентификатор организации исполнителей")
    private UUID organizationId;

    /**
     * Массив исполнителей.
     */
    @NotNull
    @Schema(description = "Исполнители")
    private List<String> executors;

    /**
     * Массив организаций заказчика.
     */
    @NotNull
    @Schema(description = "Организации")
    private List<String> organizations;

    /**
     * Массив подразделений заказчика.
     */
    @Schema(description = "Подразделения")
    private List<String> departments;

    /**
     * Массив заказчиков.
     */
    @Schema(description = "Заказчики")
    private List<String> customers;

    /**
     * Массив территорий заказчика.
     */
    @Schema(description = "Территорий заказчика")
    private List<String> geoZones;

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
