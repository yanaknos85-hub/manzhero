package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param historyType type of history.
 * @param sum sum of action.
 * @param year year of limit.
 * @param sourceLimit source limit.
 * @param sourcePeriod source period.
 * @param sourceTransportType source type of transport.
 * @param targetLimit target limit.
 * @param targetPeriod target period.
 * @param targetTransportType target type of transport.
 */
public record LimitTransferHistoryV2DTO(
        @NotNull
        Integer year,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        UUID sourceLimit,
        UUID targetLimit,
        TransportTypeEnum sourceTransportType,
        TransportTypeEnum targetTransportType,
        @JsonSerialize(using = PeriodSerializer.class)
        Period sourcePeriod,
        @JsonSerialize(using = PeriodSerializer.class)
        Period targetPeriod,
        LimitTransferHistoryType historyType
) {
}
