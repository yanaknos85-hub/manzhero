package ru.sberbank.ditsib.transport.approvals.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.transport.approvals.constant.ApprovalJournalType;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Журнал согласований
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "approval_journal")
public class ApprovalJournal {

    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор записи о согласовании
     */
    @NotNull
    private UUID approvalId;

    /**
     * Идентификатор участника
     */
    @NotNull
    private UUID actorId;

    /**
     * Идентификатор сущности
     */
    @NotNull
    private UUID actionId;

    /**
     * Статус согласования
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    /**
     * Дата и время создания согласования
     */
    @NotNull
    private LocalDateTime creationTime;

    /**
     * Идентификатор сотрудника, выполнившего согласование
     */
    private UUID approvedById;

    /**
     * Тип транспорта
     */
    @NotNull
    private String transportType;

    /**
     * Класс такси
     */
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;

    /**
     * Желаемая дата и время поездки
     */
    @NotNull
    private LocalDateTime desiredDate;

    /**
     * Идентификатор цели поездки
     */
    @NotNull
    private UUID tripPurposeId;

    /**
     * Ожидаемая стоимость поездки
     */
    private double expectedCost;

    /**
     * Ожидаемое время в пути (в секундах)
     */
    @Column(columnDefinition = "int8 (Types#BIGINT)")
    private Duration expectedTime;

    /**
     * Ожидаемая дистанция поездки
     */
    private double expectedDistance;

    /**
     * Человеко-читаемый идентификатор согласования
     */
    @Size(max = 255)
    private String humanReadableId;

    /**
     * Количество пассажиров
     */
    private int passengerCount;

    /**
     * Путевые точки маршрута в формате JSON
     */
    @Type(JsonBinaryType.class)
    private List<Map<String, Object>> waypoints = new ArrayList<>();

    /**
     * Идентификатор совместной поездки
     */
    private UUID sharedRideId;

    /**
     * Идентификатор заявки, добавленной к поездке
     */
    private UUID addRequestId;

    /**
     * Временная зона
     */
    private String timeZone;

    /**
     * Тип записи журнала
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private ApprovalJournalType type;
}
