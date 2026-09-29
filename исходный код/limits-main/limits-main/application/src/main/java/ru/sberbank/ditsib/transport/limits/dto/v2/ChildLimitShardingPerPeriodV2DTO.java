package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodDeserializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChildLimitShardingPerPeriodV2DTO(
    UUID id,
    UUID authorId,
    LocalDateTime creationTime,
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    BigDecimal sum,
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    BigDecimal balance,
    @JsonDeserialize(using = PeriodDeserializer.class)
    Period period,
    UUID limitSharingId,
    UUID organizationId
) {
}
