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

/**
 * DTO for adding request.
 *
 * @param year year of limit.
 * @param sum sum of limit.
 * @param transportType type of transport.
 * @param period period of limit.
 * @param description description of limit.
 */
public record EmpLimitRequestV2DTO(
        @NotNull
        @Min(1970)
        int year,
        @NotNull
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period period,
        @NotNull
        TransportTypeEnum transportType,
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        String description) {
}
