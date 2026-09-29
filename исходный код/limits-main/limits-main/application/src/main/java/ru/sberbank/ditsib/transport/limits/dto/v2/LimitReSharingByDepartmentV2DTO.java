package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param sum sum for resharing.
 * @param year year of limit.
 * @param sourceDepartmentId ID of department from which resharing will be performed.
 * @param sourceTransportType type of transport from which resharing will be performed.
 * @param fromPeriod period from which resharing will be performed.
 * @param toPeriod period to which resharing will be performed.
 * @param targetTransportType type of transport to which resharing will be performed.
 * @param targetDepartmentId ID of department to which resharing will be performed.
 */
public record LimitReSharingByDepartmentV2DTO(
        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        @NotNull
        Integer year,
        @NotNull
        UUID sourceDepartmentId,
        @NotNull
        TransportTypeEnum sourceTransportType,
        @NotNull
        UUID targetDepartmentId,
        @NotNull
        TransportTypeEnum targetTransportType,
        @JsonSerialize(using = PeriodSerializer.class)
        Period fromPeriod,
        @JsonSerialize(using = PeriodSerializer.class)
        Period toPeriod
) {
}
