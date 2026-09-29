package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Database entity of approver.
 */
@Entity
@Table(schema = "limits", name = "approver")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class Approver {
    
    /**
     * Id of fill request
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Employee authorized to approve fill request
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;
    
    /**
     * Employee authorized to approve fill request
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    /**
     * Employee authorized to approve personal request
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "limit_request_id")
    private LimitRequest limitRequest;
    
    /**
     * Sum requested for limit
     */
    @Column(nullable = false, name = "sum")
    @Builder.Default
    private BigDecimal sum = BigDecimal.ZERO;
    
    /**
     * State of approval
     */
    @Column(name = "approval_state")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private ApprovalState approvalState = ApprovalState.AWAITING_APPROVAL;
    
    /**
     * Date and time or personal request approval
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    /**
     * Organization id.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
}
