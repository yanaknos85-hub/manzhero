package ru.sberbank.ditsib.transport.approvals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO with cancel reason
 */
@Getter
@Setter
@Schema(title = "Отмена", description = "Данные с причинами отмены")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDTO {
    /**
     * Reason.
     */
    @NotBlank
    @Schema(description = "Причина отмены", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
    
}
