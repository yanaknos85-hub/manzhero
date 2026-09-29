package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodDeserializer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param sum sum of resharing.
 * @param targetTransportType target transport type.
 * @param sourceTransportType source transport type.
 * @param fromPeriod source period.
 * @param sourceLimitId ID of source limit.
 * @param targetLimitId ID of target limit.
 * @param toPeriod target period.
 */
public record LimitResharingV2DTO(
        @NotNull
        @Min(0)
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        @NotNull
        UUID sourceLimitId,
        TransportTypeEnum sourceTransportType,
        @NotNull
        UUID targetLimitId,
        @NotNull
        TransportTypeEnum targetTransportType,
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period fromPeriod,
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period toPeriod) {
}
