package ru.sberbank.ditsib.transport.approvals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO count
 */
@Getter
@Setter
@Schema(title = "Количество", description = "Количество согласований")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountResponseDTO {
    
    @NotBlank
    @Schema(description = "Количество", requiredMode = Schema.RequiredMode.REQUIRED)
    long count;
}
