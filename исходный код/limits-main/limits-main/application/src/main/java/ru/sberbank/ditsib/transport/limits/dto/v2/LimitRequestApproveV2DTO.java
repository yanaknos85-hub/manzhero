package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;

public record LimitRequestApproveV2DTO(
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        ApprovalState approvalState
) {
}
