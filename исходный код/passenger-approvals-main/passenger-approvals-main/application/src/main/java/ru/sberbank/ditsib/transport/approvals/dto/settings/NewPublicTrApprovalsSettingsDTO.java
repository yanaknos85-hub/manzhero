package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Новые данные настроек согласования поездок на общественном транспорте
 */
@Setter
@Getter
@Schema(
        title = "Новые данные настроек согласования поездок на общественном транспорте"
)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "organizationId"})
public class NewPublicTrApprovalsSettingsDTO extends NewTaxiApprovalsSettingsDTO {
    /** Необходимость проверки документа на этапе "Создание" */
    @Builder.Default
    @Schema(description = "Необходимость проверки документа на этапе \"Создание\"")
    private boolean approvalDocumentCheck = true;
    
    /** Необходимость этапа утверждения */
    @Builder.Default
    @Schema(description = "Необходимость этапа утверждения")
    private boolean affirmativeActive = true;
    
    /** Необходимость этапа подтверждения */
    @Builder.Default
    @Schema(description = "Необходимость этапа подтверждения")
    private boolean tripConfirmationActive = true;
    
    /** Необходимость проверки документа на этапе "Подтверждение поездки" */
    @Builder.Default
    @Schema(description = "Необходимость проверки документа на этапе \"Подтверждение поездки\"")
    private boolean tripConfirmationDocumentCheck = true;
}
