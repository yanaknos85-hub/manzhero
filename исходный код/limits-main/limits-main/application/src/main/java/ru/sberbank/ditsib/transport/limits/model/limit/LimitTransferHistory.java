package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
@Table(schema = "limits", name = "limit_transfer_history")
@Data
public class LimitTransferHistory {
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Author of sharing
     */
    @Column(name = "author_id", nullable = false)
    private UUID author;
    
    /**
     * Date and time or record creation.
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    /**
     * Year
     */
    @Column(name = "year")
    private Integer year;
    
    /**
     * Sum
     */
    @Column(name = "sum", nullable = false)
    private BigDecimal sum;
    
    /**
     * Source limit id
     */
    @Column(name = "source_limit_id", nullable = false)
    private UUID sourceLimitId;
    
    /**
     * Target limit id
     */
    @Column(name = "target_limit_id", nullable = false)
    private UUID targetLimitId;
    
    /**
     * Source transport type
     */
    @Column(name = "source_transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum sourceTransportType;
    
    /**
     * Target transport type
     */
    @Column(name = "target_transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum targetTransportType;
    
    /**
     * Исходный период
     */
    @Column(name = "source_period")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData sourcePeriod;
    
    /**
     * Целевой период
     */
    @Column(name = "target_period")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData targetPeriod;
    
    /**
     * Year
     */
    @Column(name = "history_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitTransferHistoryType historyType;
    
    /**
     * Organization id.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Limit sharing type
     */
    @Column(name = "limit_service_type")
    private String limitServiceType;
}
