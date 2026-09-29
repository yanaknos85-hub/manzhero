package ru.sberbank.ditsib.transport.approvals.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO Настройки согласований заявок на поездки для всех типов транспорта, кроме такси и общественного.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Настройки согласований заявок на поездки для всех типов транспорта, кроме такси и общественного")
public class OtherTrTypesApprovalsSettingsDTO extends ApprovalsSettingsDTO {
    
    /** Необходимость этапа утверждения поездки */
    @Builder.Default
    @Schema(description = "Необходимость этапа утверждения поездки")
    private boolean tripApprovalActive = true;
}
