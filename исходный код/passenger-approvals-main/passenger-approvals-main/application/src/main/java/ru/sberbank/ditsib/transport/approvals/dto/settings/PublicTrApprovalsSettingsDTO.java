package ru.sberbank.ditsib.transport.approvals.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO Настройки согласований заявок на компенсацию общественный транспорт.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Настройки согласований заявок на компенсацию общественный транспорт")
public class PublicTrApprovalsSettingsDTO extends ApprovalsSettingsDTO {
    
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
