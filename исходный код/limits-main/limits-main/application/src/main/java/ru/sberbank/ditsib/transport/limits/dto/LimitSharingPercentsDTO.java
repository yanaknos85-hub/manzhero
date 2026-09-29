package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Погодичное процентное распределение",
        description = "Погодичное процентное распределение")
public class LimitSharingPercentsDTO {

    /**
     * Month0
     */
    @NotNull
    @Schema(description = "Месяц 0")
    @JsonProperty("month0")
    private Integer january;

    /**
     * Month1
     */
    @NotNull
    @Schema(description = "Месяц 1")
    @JsonProperty("month1")
    private Integer february;

    /**
     * Month2
     */
    @NotNull
    @Schema(description = "Месяц 2")
    @JsonProperty("month2")
    private Integer march;

    /**
     * Month3
     */
    @NotNull
    @Schema(description = "Месяц 3")
    @JsonProperty("month3")
    private Integer april;

    /**
     * Month4
     */
    @NotNull
    @Schema(description = "Месяц 4")
    @JsonProperty("month4")
    private Integer may;

    /**
     * Month5
     */
    @NotNull
    @Schema(description = "Месяц 5")
    @JsonProperty("month5")
    private Integer june;

    /**
     * Month6
     */
    @NotNull
    @Schema(description = "Месяц 6")
    @JsonProperty("month6")
    private Integer july;

    /**
     * Month7
     */
    @NotNull
    @Schema(description = "Месяц 7")
    @JsonProperty("month7")
    private Integer august;

    /**
     * Month8
     */
    @NotNull
    @Schema(description = "Месяц 8")
    @JsonProperty("month8")
    private Integer september;

    /**
     * Month9
     */
    @NotNull
    @Schema(description = "Месяц 9")
    @JsonProperty("month9")
    private Integer october;

    /**
     * Month10
     */
    @NotNull
    @Schema(description = "Месяц 10")
    @JsonProperty("month10")
    private Integer november;

    /**
     * Month11
     */
    @NotNull
    @Schema(description = "Месяц 11")
    @JsonProperty("month11")
    private Integer december;

    /**
     * Limit limit
     */
    @Schema(description = "Лимит")
    private UUID limitId;
}
