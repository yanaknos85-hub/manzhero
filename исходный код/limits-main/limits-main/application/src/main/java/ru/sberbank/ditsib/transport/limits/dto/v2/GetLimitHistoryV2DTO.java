package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param year year of limit history.
 * @param sum sum of limit.
 * @param id ID of limit.
 * @param period period of limit.
 * @param creationTime time of limit created.
 * @param limitServiceType service type of limit.
 * @param organizationId ID of organization.
 * @param authorId ID of author.
 * @param counterpartLimitId target limit.
 * @param counterpartPeriod target period.
 * @param counterpartTransportType target transport type.
 * @param historyType type of history.
 * @param limitId ID of limit.
 * @param limitSharingBalance shared sum.
 * @param limitSharingPerperiodBalance shared balance per period.
 * @param limitSharingPerperiodSum shared sum per period.
 * @param limitSharingSum sum of limit sharing.
 * @param transportType type of transport.
 */
public record GetLimitHistoryV2DTO(
        UUID id,
        UUID limitId,
        TransportTypeEnum transportType,
        UUID authorId,
        UUID organizationId,
        LocalDateTime creationTime,
        Integer year,
        @JsonSerialize(using = PeriodSerializer.class)
        Period period,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        UUID counterpartLimitId,
        TransportTypeEnum counterpartTransportType,
        @JsonSerialize(using = PeriodSerializer.class)
        Period counterpartPeriod,
        LimitHistoryType historyType,
        String limitServiceType,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal limitSharingSum,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal limitSharingBalance,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal limitSharingPerperiodSum,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal limitSharingPerperiodBalance
) {
}
