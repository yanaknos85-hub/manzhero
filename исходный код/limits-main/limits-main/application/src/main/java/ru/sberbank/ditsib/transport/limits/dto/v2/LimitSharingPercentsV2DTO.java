package ru.sberbank.ditsib.transport.limits.dto.v2;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for adding request.
 * @param limitId ID of limits.
 * @param april percents for april.
 * @param august percents for august.
 * @param december percents for december.
 * @param february percents for february.
 * @param january percents for january.
 * @param july percents for july.
 * @param june percents for june.
 * @param march percents for march.
 * @param may percents for may.
 * @param november percents for november.
 * @param october percents for october.
 * @param september percents for september.
 */
public record LimitSharingPercentsV2DTO(
        @NotNull
        Integer january,
        @NotNull
        Integer february,
        @NotNull
        Integer march,
        @NotNull
        Integer april,
        @NotNull
        Integer may,
        @NotNull
        Integer june,
        @NotNull
        Integer july,
        @NotNull
        Integer august,
        @NotNull
        Integer september,
        @NotNull
        Integer october,
        @NotNull
        Integer november,
        @NotNull
        Integer december,
        UUID limitId
) {
}
