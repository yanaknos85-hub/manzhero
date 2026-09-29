package ru.sberbank.ditsib.transport.limits.model.limit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "limits", name = "limit_sharing_per_period")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class LimitSharingPerPeriod implements HasPeriod {
    
    /**
     * Unique idetificator.
     */
    @Id
    @GeneratedValue
    @ToString.Include
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
    @ToString.Include
    private LocalDateTime creationTime;
    
    /**
     * Sum
     */
    @Column(name = "sum", nullable = false)
    @ToString.Include
    private BigDecimal sum;
    
    /**
     * Balance
     */
    @Column(name = "balance", nullable = false)
    @ToString.Include
    private BigDecimal balance;
    
    /**
     * LimitSharing id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_sharing_id", nullable = false)
    private LimitSharing limitSharing;

    @OneToMany(mappedBy = "limitSharingPerPeriod")
    @Builder.Default
    private List<LimitSpending> spendings = new ArrayList<>();
    
    // setLimitSharing
    @PrePersist
    void create() {
        setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
    }
    
    /**
     * Month. If present - limit can be spent...
     */
    @Column(name = "period", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @ToString.Include
    private PeriodData periodData;

    @Column(name = "additional", nullable = false)
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal additionalSum;
}
