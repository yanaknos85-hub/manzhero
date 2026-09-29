package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Настройки цветов лимитов", description = "Настройки цветов лимитов")
public class LimitColorsDTO {

    @JsonProperty("EMP_LIMIT_GREEN_FROM")
    @Schema(description = "Личные лимиты зеленый от")
    private Integer empLimitGreenFrom;

    @JsonProperty("EMP_LIMIT_GREEN_UNTIL")
    @Schema(description = "Личные лимиты зеленый до")
    private Integer empLimitGreenUntil;

    @JsonProperty("EMP_LIMIT_YELLOW_FROM")
    @Schema(description = "Личные лимиты желтый от")
    private Integer empLimitYellowFrom;

    @JsonProperty("EMP_LIMIT_YELLOW_UNTIL")
    @Schema(description = "Личные лимиты желтый до")
    private Integer empLimitYellowUntil;

    @JsonProperty("EMP_LIMIT_RED_FROM")
    @Schema(description = "Личные лимиты красный от")
    private Integer empLimitRedFrom;

    @JsonProperty("EMP_LIMIT_RED_UNTIL")
    @Schema(description = "Личные лимиты красный до")
    private Integer empLimitRedUntil;

    @JsonProperty("DEP_LIMIT_GREEN_FROM")
    @Schema(description = "Лимиты подразделений зеленый от")
    private Integer depLimitGreenFrom;

    @JsonProperty("DEP_LIMIT_GREEN_UNTIL")
    @Schema(description = "Лимиты подразделений зеленый до")
    private Integer depLimitGreenUntil;

    @JsonProperty("DEP_LIMIT_YELLOW_FROM")
    @Schema(description = "Лимиты подразделений желтый от")
    private Integer depLimitYellowFrom;

    @JsonProperty("DEP_LIMIT_YELLOW_UNTIL")
    @Schema(description = "Лимиты подразделений желтый до")
    private Integer depLimitYellowUntil;

    @JsonProperty("DEP_LIMIT_RED_FROM")
    @Schema(description = "Лимиты подразделений красный от")
    private Integer depLimitRedFrom;

    @JsonProperty("DEP_LIMIT_RED_UNTIL")
    @Schema(description = "Лимиты подразделений красный до")
    private Integer depLimitRedUntil;
}
