package ru.sberbank.ditsib.transport.limits.model.limit;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestAskTargets;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity describing request
 */
@Entity
@Table(schema = "limits", name = "limit_request")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "approverList" })
public class LimitRequest implements HasPeriod {
    
    /**
     * Id of personal request
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Human readable id
     */
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    /**
     * Creator of personal request
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Employee author;
    
    /**
     * Date and time or personal request creation
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    /**
     * Transport type
     */
    @Column(name = "transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum transportType;
    
    /**
     * Year
     */
    @Column(name = "year")
    private Integer year;
    
    /**
     * Month
     */
    @Column(name = "period")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData periodData;
    
    /**
     * Status of personal request
     */
    @Column(name = "request_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitRequestStatus status = LimitRequestStatus.INIT;
    
    /**
     * Status code
     */
    @Column(name = "status_code")
    private Integer statusCode;
    
    /**
     * Type of limit for request
     */
    @Column(name = "limit_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitType limitType;
    
    /**
     * Sum requested for limit
     */
    @Column(nullable = false, name = "sum")
    private BigDecimal sum;
    
    /**
     * Text for limit personal request
     */
    @Column(name = "description")
    private String description;
    
    /**
     * Decline reason
     */
    @Column(name = "decline_reason")
    private String declineReason;
    
    /**
     * Ask targets
     */
    @Column(name = "ask_targets")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitRequestAskTargets askTargets;
    
    /**
     * List of assigned approval
     */
    @OneToMany(targetEntity = Approver.class, mappedBy = "limitRequest",
               fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Approver> approverList = new ArrayList<>();
    
    /**
     * Organization id.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
}
