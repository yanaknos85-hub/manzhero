package ru.sber.transport.limits.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Модель лимита
 */
@Data
public class Limit {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Сумма
     */
    private BigDecimal sum;

    /**
     * Баланс
     */
    private BigDecimal reserve;

    /**
     * Статус
     */
    private Status status;

    /**
     * Идентификатор родительского лимита
     */
    private UUID parentId;

    /**
     * Идентификатор организации
     */
    private UUID organizationId;

    /**
     * Время создания.
     */
    private OffsetDateTime creationTime;

    /**
     * Экономия.
     */
    private BigDecimal economy;

    /**
     * Конечное распределение.
     */
    private boolean finalSharing;

    /**
     * Человекочитаемый идентификатор
     */
    private String humanReadableId;

    /**
     * Идентификатор владельца
     */
    private UUID ownerId;

    /**
     * Вид услуги
     */
    private String serviceType;

    /**
     * Вид распределения
     */
    private SharingType sharingType;

    /**
     * Признак запрета создания нижестоящих лимитов
     */
    private boolean useThisLimit;

    /**
     * Год действия лимита.
     */
    private int year;

    /**
     * Время обновления.
     */
    private OffsetDateTime updateTime;

    /**
     * Список ответственных за лимит.
     */
    private List<UUID> responsibles = new ArrayList<>();

    /**
     * Хэш лимита.
     */
    private String hash;

    /**
     * Идентификатор подразделения
     */
    private UUID departmentId;

    /**
     * Идентификатор сотрудника личного лимита
     */
    private UUID employeeId;

    /**
     * Тип лимита
     */
    private Type type;
}
