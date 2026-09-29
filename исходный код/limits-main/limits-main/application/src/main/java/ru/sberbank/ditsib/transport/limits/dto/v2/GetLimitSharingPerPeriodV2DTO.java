package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.serializer.PeriodDeserializer;
import ru.sberbank.ditsib.transport.limits.serializer.PeriodSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param sum full sum of sharing.
 * @param id ID of sharing per period.
 * @param author author of sharing per period.
 * @param balance remains sum.
 * @param creationTime time when sharing created.
 * @param limitSharing ID of sharing.
 * @param period period of sharing.
 * @param sumReservedForCurrentPeriod reserved for current period.
 * @param sumResharingsPeriod sum for resharing.
 */
public record GetLimitSharingPerPeriodV2DTO(
        @NotNull
        UUID id,
        @NotNull
        UUID author,
        @NotNull
        LocalDateTime creationTime,
        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal balance,
        @NotNull
        @JsonSerialize(using = PeriodSerializer.class)
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period period,
        UUID limitSharing,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sumReservedForCurrentPeriod,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sumResharingsPeriod
        ) {
}

