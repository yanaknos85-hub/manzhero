package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodDeserializer;

import java.math.BigDecimal;
import java.util.UUID;

public record RemainsPerPeriodV2DTO(
        UUID requestId,
        UUID limitId,
        LimitType limitType,
        String limitHumanId,
        UUID departmentId,
        @JsonDeserialize(using = PeriodDeserializer.class)
        Period period,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal balancePerPeriod
) {
}

    

    

