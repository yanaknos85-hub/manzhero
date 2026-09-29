package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.Immutable;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "limits", name = "limit_stats_spending")
@Data
@Immutable
public class LimitStatisticSpending implements HasPeriod {

    @Id
    @Column(name = "period_sharing_id")
    private UUID periodSharingId;

    @Column(name = "sharing_id")
    private UUID sharingId;

    @Column(name = "spent")
    private BigDecimal sum;

    @Column(name = "period")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData periodData;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum transportType;

    @Column
    private int year;

}
