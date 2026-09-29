package ru.sberbank.ditsib.transport.approvals.dto.settings;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * DTO Настройки согласований заявок
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"organizationId"})
@Schema(title = "Настройки согласований заявок")
public class ApprovalsSettingsDTO {
    
    /** ID настройки */
    @Schema(description = "ID настройки")
    private UUID id;
    
    /** ID организации */
    @Schema(description = "ID организации")
    private UUID organizationId;
    
    /** Необходимость этапа согласования */
    @Builder.Default
    @Schema(description = "Необходимость этапа согласования")
    private boolean approvalActive = true;
    
    /** Сумма, не требующая согласования */
    @Builder.Default
    @Schema(description = "Сумма, не требующая согласования")
    private long minCostToBeApproved = 0;
    
    /** Тип транспорта */
    @Schema(description = "Тип транспорта")
    private String transportType;
    
    /** Список элементов настроек по региону и целям поездки */
    @Schema(description = "Список элементов настроек по региону и целям поездки ")
    private List<PurposeAndRegionApprovalSettingsItemDTO> purposeAndRegionItems;
}
