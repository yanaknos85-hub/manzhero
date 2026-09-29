package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param transportType type of transport for sharing.
 * @param sum full sum of sharing.
 * @param limitId ID of limit.
 * @param creationTime time of sharing created.
 * @param id ID of sharing.
 * @param balance remains sum of sharing.
 * @param author ID of author.
 * @param sharings periods.
 * @param sumResharingsYear reshared sum at the year.
 */
public record GetLimitSharingV2DTO(

        @NotNull
        UUID id,

        @NotNull
        UUID author,

        @NotNull
        LocalDateTime creationTime,

        @NotNull
        TransportTypeEnum transportType,

        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,

        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal balance,

        UUID limitId,

        List<GetLimitSharingPerPeriodV2DTO> sharings,
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sumResharingsYear
        ) {
}

