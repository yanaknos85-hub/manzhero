package ru.sberbank.ditsib.transport.approvals.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO цели поездки
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id"})
@Schema(title = "Цель поездки", description = "Цель поездки")
public class TripPurposeDTO {
    
    /** ID цели */
    @NotNull
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /** Описание */
    @NotBlank
    @Schema(description = "label", requiredMode = Schema.RequiredMode.REQUIRED)
    private String label;
}
