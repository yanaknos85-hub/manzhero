package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.validation.interval.Interval;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity of delegate
 */
@Entity
@Table(schema = "corporate", name = "delegate")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Interval(startField = "startDate", endField = "endDate", inclusion = Interval.Include.INCLUDE)
public class DelegateRecord {

    /**
     * Уникальный идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Начальник
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    /**
     * Делегат
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Employee delegate;

    /**
     * Начало действия делегирования
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Окончание действия делегирования
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Тип транспорта
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private TransportTypeEnum transportType;

    /**
     * Статус записи: INACTIVE - удалена (softDelete), ACTIVE - активна
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RecordStatus status = RecordStatus.ACTIVE;

    /**
     * Действия перед сохранением
     */
    @PrePersist
    public void prePersist() {
        setDefaultActive();
    }

    /**
     * Действия перед обновлением
     */
    @PreUpdate
    public void preUpdate() {
        setDefaultActive();
    }

    private void setDefaultActive() {
        if (status == null) {
            status = RecordStatus.ACTIVE;
        }
    }
}
