package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestAskTargets;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodDeserializer;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param sum sum of limit request.
 * @param transportType type of transport.
 * @param year year of limit request.
 * @param period period of request.
 * @param description description of request.
 * @param askTargets direction of resharing.
 * @param departments set of departments.
 */
public record DepLimitRequestV2DTO(

        @NotNull
        int year,

        @JsonDeserialize(using = PeriodDeserializer.class)
        @NotNull
        Period period,

        @NotNull
        TransportTypeEnum transportType,

        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,

        String description,

        LimitRequestAskTargets askTargets,

        Set<UUID> departments
){}
