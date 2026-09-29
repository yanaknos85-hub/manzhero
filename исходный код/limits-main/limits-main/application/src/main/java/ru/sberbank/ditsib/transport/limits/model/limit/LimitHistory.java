package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
@Table(schema = "limits", name = "limit_history")
@Data
public class LimitHistory {
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Source limit id
     */
    @Column(name = "limit_id", nullable = false)
    private UUID limitId;
    
    /**
     * Source transport type
     */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum transportType;
    
    /**
     * Author of sharing
     */
    @Column(name = "author_id", nullable = false)
    private UUID authorId;
    
    /**
     * Organization id.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
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
     * Исходный период
     */
    @Column(name = "period")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData period;
    
    /**
     * Sum
     */
    @Column(name = "sum", nullable = false)
    private BigDecimal sum;
    
    /**
     * Counterpart limit id
     */
    @Column(name = "counterpart_limit_id", nullable = false)
    private UUID counterpartLimitId;
    
    /**
     * Target transport type
     */
    @Column(name = "counterpart_transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum counterpartTransportType;
    
    /**
     * Целевой период
     */
    @Column(name = "counterpart_period")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData counterpartPeriod;
    
    /**
     * Year
     */
    @Column(name = "history_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitHistoryType historyType;
    
    /**
     * Limit sharing type
     */
    @Column(name = "service_type")
    private String limitServiceType;
    
    /**
     * Limit sharing type
     */
    @Column(name = "limit_sum")
    private BigDecimal limitSum;
    
    /**
     * Limit sharing type
     */
    @Column(name = "limit_sharing_sum")
    private BigDecimal limitSharingSum;
    
    /**
     * Limit sharing type
     */
    @Column(name = "limit_sharing_balance")
    private BigDecimal limitSharingBalance;
    
    /**
     * Limit sharing type
     */
    @Column(name = "limit_sharing_per_period_sum")
    private BigDecimal limitSharingPerPeriodSum;
    
    /**
     * Limit sharing type
     */
    @Column(name = "limit_sharing_per_period_balance")
    private BigDecimal limitSharingPerPeriodBalance;
}
