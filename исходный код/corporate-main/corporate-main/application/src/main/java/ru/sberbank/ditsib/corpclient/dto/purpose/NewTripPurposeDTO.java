package ru.sberbank.ditsib.corpclient.dto.purpose;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import ru.sberbank.ditsib.transport.constants.TripPurposeType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object with data about new trip purpose.
 */
@Jacksonized
@Getter
@Builder
@Schema(title = "Новая цель поездки", description = "Описание цели поездки по заявке")
public class NewTripPurposeDTO {
    
    /**
     * Trip purpose
     */
    @NotBlank
    @Schema(description = "Описание цели поездки", maxLength = 128)
    @Size(max = 128)
    private final String label;
    
    @Builder.Default
    @Schema(description = "Тип цели (Личная/Корпоративная)")
    private final TripPurposeType purposeType = TripPurposeType.CORPORATE;
    
    /**
     * Organization
     */
    @Schema(description = "Организация")
    @JsonIgnore
    private final UUID organization;
    
    @Singular("tripPurposeAttribute")
    @Schema(description = "Аттрибуты")
    private final List<@Valid IdContainerDTO> tripPurposeAttributes;
    
    @Singular("tripPurposeDepartment")
    @Schema(description = "Департамент")
    private final List<@Valid IdContainerDTO> tripPurposeDepartments;
    
    @Singular("tripPurposeDate")
    @Schema(description = "Даты")
    private final List<@Valid TripPurposeDateDTO> tripPurposeDates;
    
    @Singular("tripPurposeTime")
    @Schema(description = "Время")
    private final List<@Valid TripPurposeTimeRangeDTO> tripPurposeTimes;
    
    @Singular("tripPurposeWeekday")
    @Schema(description = "Недели")
    private final List<@Valid TripPurposeWeekdayDTO> tripPurposeWeekdays;

    @Schema(description = "Константное значение для иконки (фронт)", maxLength = 128)
    @Size(max = 128)
    private final String icon;
}
