package ru.sberbank.ditsib.transport.approvals.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Базовый класс для всех согласований
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public abstract class Approval {

    /**
     * Время окончания согласования
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * Type of transport used for request
     */
    @Column(name = "transport_type", nullable = false)
    private String transportType;
    
    /**
     * Type of transport used for request
     */
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(name = "public_compensation_type")
    private PublicCompensationType publicCompensationType;
    
    /**
     * Class of taxi used for request
     */
    @Column(name = "trip_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
    
    /**
     * Desired date and time of trip
     */
    @Column(name = "desired_date", nullable = false)
    private LocalDateTime desiredDate;
    
    /**
     * Purpose string description.
     */
    @Column(name = "trip_purpose_id", nullable = false)
    private UUID purposeId;
    
    /**
     * ID тарифа
     */
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    /**
     * Стоимость
     */
    @Column(name = "expected_cost")
    private double cost;

    /**
     * Маршрут
     */
    @Type(JsonBinaryType.class)
    @Column(name = "waypoints")
    private List<Map<String, Object>> waypoints = new ArrayList<>();

    /**
     * Ожидаемое время в пути
     */
    @Column(name = "expected_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration expectedTime;

    /**
     * Ожидаемое расстояние
     */
    @Column(name = "expected_distance")
    private double expectedDistance;

    /**
     * Количество пассажиров
     */
    @Column(name = "passenger_count")
    private int passengerCount;

    /**
     * Человеко-читаемый ID заявки
     */
    @Column(name = "human_readable_id")
    private String requestHumanReadableId;

    /**
     * Пассажир
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", insertable = false, updatable = false)
    private Employee passenger;

    /**
     * Идентификатор согласования
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * ID сотрудника, который создал заявку
     */
    @Column(name = "author_id")
    private UUID authorId;

    /**
     * ID сотрудника, который согласовал заявку
     */
    @Column(name = "actor_id")
    private UUID actorId;
    
    /**
     * Id заявки
     */
    @Column(name = "action_id")
    private UUID actionId;

    /**
     * Статус заявки
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    /**
     * Время создания заявки
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    /**
     * Контрольный срок исполнения заявки
     */
    @Column(name = "deadline")
    private LocalDateTime deadline;

    /**
     * Идентификатор сотрудника, который согласовал заявку
     */
    @Column(name = "approved_by_id")
    private UUID approvedById;

    /**
     * Причина отказа
     */
    @Column(name = "reason")
    private String reason;

    /**
     * Временная зона заявки
     */
    @Column(name = "time_zone")
    private String timeZone;

    /**
     * Список фродовых данных
     */
    @OneToMany(
            mappedBy = "approval",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<FraudData> fraudData = new ArrayList<>();

    /**
     * Тип заявки
     * @return Тип заявки
     */
    @Transient
    public abstract ApprovalType getType();
}
