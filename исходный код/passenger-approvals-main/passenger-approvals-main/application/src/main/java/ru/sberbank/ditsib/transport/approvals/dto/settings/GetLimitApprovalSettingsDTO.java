package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO Настройки согласований заявок на лимит для get-запроса
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"organizationId"})
@Schema(title = "Настройки согласований заявок на лимит", description = "Для get-запроса")
public class GetLimitApprovalSettingsDTO {
    
    /** ID корп.клиента */
    @Schema(description = "ID корп.клиента")
    private UUID organizationId;
    
    /** Необходимость этапа согласования */
    @Builder.Default
    @Schema(description = "Необходимость этапа согласования")
    private boolean approvalActive = true;
    
    /** Сумма, не требующая согласования */
    @Builder.Default
    @Schema(description = "Сумма, не требующая согласования")
    private Integer minCostToBeApproved = 0;
}
