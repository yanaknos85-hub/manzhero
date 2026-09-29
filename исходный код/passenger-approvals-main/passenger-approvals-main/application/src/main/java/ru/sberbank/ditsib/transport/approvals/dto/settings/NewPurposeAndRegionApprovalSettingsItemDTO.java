package ru.sberbank.ditsib.transport.approvals.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Новый элемент настройки согласований заявок по цели и региону
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Новый элемент настройки согласований заявок по цели и региону")
public class NewPurposeAndRegionApprovalSettingsItemDTO {
    
    /**
     * Цель поездки
     */
    @NotNull
    @Schema(description = "Идентификатор цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID purposeId;
    
    /** Геозона. Если не указана, то настройка распространяется на все регионы */
    @Schema(description = "ID геозоны")
    private UUID regionId;
    
    /** Сумма, не требующая согласования */
    @Builder.Default
    @Schema(description = "Сумма, не требующая согласования, коп.")
    private int minCostToBeApproved = 0;
}
