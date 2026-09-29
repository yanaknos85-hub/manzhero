package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "limits", name = "sharings")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "sharingPerPeriods", "limit" })
public class LimitSharing {
    
    /**
     * Unique idetificator.
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Author of sharing
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Employee author;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    /**
     * Sum
     */
    @Column(name = "sum", nullable = false)
    private BigDecimal sum;
    
    /**
     * Balance
     */
    @Column(name = "remains", nullable = false)
    private BigDecimal balance;
    
    /**
     * Limit transport type
     */
    @Column(name = "transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum transportType;
    
    /**
     * Limit id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id", nullable = false)
    private Limit limit;
    
    /**
     * Флаг распределенности
     */
    @Builder.Default
    @Column
    private boolean distributed = false;
    
    @OneToMany(mappedBy = "limitSharing", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter
    private List<LimitSharingPerPeriod> sharingPerPeriods = new ArrayList<>();
    
}
