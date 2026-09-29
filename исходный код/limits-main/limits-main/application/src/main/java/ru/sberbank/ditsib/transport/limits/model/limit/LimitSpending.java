package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "limits", name = "spendings")
@Data
@EqualsAndHashCode(of = "id")
@ToString
public class LimitSpending {
    
    /**
     * Unique idetificator.
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Employee of spending
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "spending_time")
    private LocalDateTime spendingTime;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;
    
    /**
     * Sum
     */
    @Column(name = "reserved", nullable = false)
    private BigDecimal sumReserved;
    
    /**
     * Sum
     */
    @Column(name = "spent")
    private BigDecimal sumSpent;
    
    /**
     * Limit transport type
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitSpendingStatus status;
    
    /**
     * Limit sharing id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_sharing_id")
    private LimitSharingPerPeriod limitSharingPerPeriod;
    
    /**
     * Transportation request id
     */
    @Column(name = "request_id", nullable = false)
    private UUID requestId;
}
