package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Новые настройки поездки на такси
 */
@Setter
@Getter
@Schema(
        title = "Новые данные настроек поездки на такси",
        description = "Новые данные настроек поездки на такси"
)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "organizationId"})
public class NewTaxiApprovalsSettingsDTO {
    
    /** Необходимость этапа согласования */
    @Builder.Default
    @Schema(description = "Необходимость этапа согласования")
    private boolean approvalActive = true;
    
    /** Сумма, не требующая согласования */
    @Builder.Default
    @Schema(description = "Сумма, не требующая согласования")
    private int minCostToBeApproved = 0;
    
    /** Список элементов настроек по региону и целям поездки */
    @Valid
    @Schema(description = "Список элементов настроек по региону и целям поездки")
    private List<NewPurposeAndRegionApprovalSettingsItemDTO> purposeAndRegionItems;
}
