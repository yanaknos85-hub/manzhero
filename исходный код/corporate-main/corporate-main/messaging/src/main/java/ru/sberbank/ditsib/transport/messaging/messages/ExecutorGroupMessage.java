package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Jacksonized
@SuperBuilder
@Getter
@Setter
public class ExecutorGroupMessage implements Message<UUID> {

    /**
     * ID.
     */
    private final UUID id;

    /**
     * Услуга.
     */
    private String service;

    /**
     * Человеко-читаемый идентификатор.
     */
    private String humanReadableId;

    /**
     * Активность группы исполнителей.
     */
    private Boolean active;

    /**
     * Название группы.
     */
    private String name;

    /**
     * Уровень сервиса.
     */
    private String serviceLevel;

    /**
     * Организация исполнителей.
     */
    private String organizationName;

    /**
     * Идентификатор организации исполнителей.
     */
    private UUID organizationId;

    /**
     * Дата и время создания в UTC.
     */
    private LocalDateTime creationTime;

    /**
     * Дата и время изменения в UTC.
     */
    private LocalDateTime updatedAt;

    /**
     * Идентификатор инициатора.
     */
    private UUID authorId;

    /**
     * Идентификатор последнего модификатора.
     */
    private UUID userId;

    /**
     * Массив организаций заказчика.
     */
    private List<Organization> organizations;

    /**
     * Массив исполнителей.
     */
    private List<Executor> executors;

    /**
     * Массив подразделений заказчика.
     */
    private List<Department> departments;

    /**
     * Массив заказчиков.
     */
    private List<Customer> customers;

    /**
     * Массив территорий заказчика.
     */
    private List<GeoZone> geoZones;

    @Jacksonized
    @Builder
    @Getter
    @Setter
    public static class GeoZone {

        /**
         * Идентификатор гео зоны.
         */
        private UUID id;

        /**
         * Наименование гео зоны.
         */
        private String name;
    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    public static class Customer {

        /**
         * Identifier
         */
        private UUID id;

        /**
         * FIO
         */
        private String name;

        /**
         * Unique personnel number
         */
        private String personnelNumber;

        /**
         * Employee status
         */
        private String status;
    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    public static class Department {

        /**
         * Identifier
         */
        private UUID id;

        /**
         * Name of department
         */
        private String departmentName;

        /**
         * Status of department.
         */
        private String status;

        /**
         * Identifier (human readable)
         */
        private String humanReadableId;
    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    public static class Organization {

        /**
         * Identifier
         */
        private UUID id;

        /**
         * Official name
         */
        private String officialName;

        /**
         * Status.
         */
        private String status;
    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    public static class Executor {

        /**
         * Идентификатор сотрудника.
         */
        private UUID employeeId;

        /**
         * ФИО.
         */
        private String employeeName;

        /**
         * Табельный номер.
         */
        private String employeePersonnelNumber;

        /**
         * Статус.
         */
        private String employeeStatus;

        /**
         * Идентификатор подразделения.
         */
        private UUID departmentId;

        /**
         * Наименование.
         */
        private String departmentName;

        /**
         * Человеко-читаемый идентификатор.
         */
        private String departmentHumanReadableId;

        /**
         * Статус.
         */
        private String departmentStatus;
    }
}
