package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param historyType type of history.
 * @param sum sum of action.
 * @param year year of limit.
 * @param source source data.
 * @param target target data.
 * @param id ID of history store.
 * @param author ID of author.
 * @param creationTime date of record created.
 */
public record GetLimitTransferHistoryV2DTO(
        @NotNull
        UUID id,
        UUID author,
        LocalDateTime creationTime,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        LimitData source,
        LimitData target,
        Integer year,
        LimitTransferHistoryType historyType) {
}
