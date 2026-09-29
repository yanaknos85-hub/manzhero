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
@Table(schema = "limits", name = "limit_stats")
@Data
@Immutable
public class LimitStatistic implements HasPeriod {

    @Id
    @Column(name = "period_sharing_id")
    private UUID periodSharingId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "sharing_id")
    private UUID sharingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum transportType;

    @Column(name = "sum")
    private BigDecimal sum;

    @Column
    private int year;

    @Column(name = "period")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PeriodData periodData;

}
