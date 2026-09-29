package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for adding request.
 *
 * @param finalSharing flag if sharing is final.
 * @param limitServiceType service type of limits.
 * @param limitSharingType type of limit sharing.
 * @param parentId ID of parent limit.
 * @param sum sum of limit.
 * @param useThisLimit flag - using only current limit.
 * @param year year ofl limit.
 */
public record DepLimitPrimaryV2DTO(
        @NotNull
        int year,
        @NotNull
        LimitSharingType limitSharingType,
        @NotNull
        String limitServiceType,
        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        BigDecimal sum,
        UUID parentId,
        boolean finalSharing,
        boolean useThisLimit
) {
}
