package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * @param sum sum for resharing.
 * @param sourceTransportType type of transport from which resharing will be performed.
 * @param fromPeriod period from which resharing will be performed.
 * @param toPeriod period to which resharing will be performed.
 * @param targetTransportType type of transport to which resharing will be performed.
 * @param limitId ID of limit.
 */
public record LimitReSharingByTransportTypeV2DTO(
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        @NotNull
        UUID limitId,
        @NotNull
        TransportTypeEnum sourceTransportType,
        @NotNull
        TransportTypeEnum targetTransportType,
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period fromPeriod,
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period toPeriod
) {

}
