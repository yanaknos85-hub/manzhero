package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Новые данные настроек согласования поездок на транспорте, кроме общественного и такси
 */
@Setter
@Getter
@Schema(
        title = "Новые данные настроек согласования поездок на транспорте, кроме общественного и такси"
)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "organizationId"})
public class NewOtherTrTypesApprovalsSettingsDTO extends NewTaxiApprovalsSettingsDTO {
    
    /** Необходимость этапа утверждения поездки */
    @Builder.Default
    @Schema(description = "Необходимость этапа утверждения поездки")
    private boolean tripApprovalActive = true;
    
    /** Тип транспорта */
    @Schema(description = "Тип транспорта", example = "PERSONAL | CARSHARING | BICYCLE | WALK | SCOOTER")
    private String transportType;
}
