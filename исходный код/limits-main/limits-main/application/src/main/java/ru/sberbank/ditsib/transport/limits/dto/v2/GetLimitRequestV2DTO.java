package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestAskTargets;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.ApproverDTO;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;
import ru.sberbank.ditsib.transport.limits.model.GetEmployeeDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetLimitRequestV2DTO(
        @NotNull
        UUID id,

        @NotNull
        String humanReadableId,
        GetEmployeeDTO author,
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
        LimitRequestStatus status,
        Integer statusCode,
        LocalDateTime creationTime,
        LimitType limitType,
        LimitRequestAskTargets askTargets,
        List<ApproverDTO> approverDtoList,
        UUID limitId,
        String limitHumanreadableid,
        LimitSharingType limitSharingType,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal plannedSum,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal limitSum,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal limitBalance,

        //--------------------------------------

        UUID parentLimitId,
        String parentLimitHumanreadableid,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal parentLimitSum,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal parentLimitBalance
) {
}
