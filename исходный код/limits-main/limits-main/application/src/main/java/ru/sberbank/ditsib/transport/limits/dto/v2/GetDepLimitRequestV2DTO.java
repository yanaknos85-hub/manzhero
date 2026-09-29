package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record GetDepLimitRequestV2DTO(

        @NotNull
        UUID id,

        @NotNull
        String humanReadableId,
        UUID authorId,
        Integer year,
        @JsonSerialize(using = PeriodSerializer.class)
        Period period,
        TransportTypeEnum transportType,
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        String description,
        String declineReason,
        String askTargets,
        Set<UUID> departments,
        ApprovalState approvalState,
        LimitRequestStatus status,
        LocalDateTime creationTime
) {
}
