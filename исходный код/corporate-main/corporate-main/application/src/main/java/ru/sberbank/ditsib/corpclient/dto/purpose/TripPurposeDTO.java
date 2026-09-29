package ru.sberbank.ditsib.corpclient.dto.purpose;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.sberbank.ditsib.transport.constants.TripPurposeType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object with data about existing trip purpose.
 */
@Data
@EqualsAndHashCode
@Schema(title = "Цель поездки", description = "Описание цели поездки по заявке")
public class TripPurposeDTO {

    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * Trip purpose
     */
    @NotBlank
    @Schema(description = "Описание цели поездки", maxLength = 128)
    @Size(max = 128)
    private String label;
    
    @Schema(description = "Тип цели (Личная/Корпоративная)")
    private TripPurposeType purposeType = TripPurposeType.CORPORATE;

    /**
     * Organization
     */
    @Schema(description = "Организация")
    @JsonIgnore
    private UUID organization;

    @Schema(description = "Аттрибуты")
    private List<@Valid TripPurposeAttributeDTO> tripPurposeAttributes;

    @Schema(description = "Департамент")
    private List<@Valid TripPurposeDepartmentDTO> tripPurposeDepartments;

    @Schema(description = "Даты")
    private List<@Valid TripPurposeDateDTO> tripPurposeDates;

    @Schema(description = "Время")
    private List<@Valid TripPurposeTimeRangeDTO> tripPurposeTimes;

    @Schema(description = "Недели")
    private List<@Valid TripPurposeWeekdayDTO> tripPurposeWeekdays;

    @Schema(description = "Константное значение для иконки (фронт)", maxLength = 128)
    @Size(max = 128)
    private String icon;

}
