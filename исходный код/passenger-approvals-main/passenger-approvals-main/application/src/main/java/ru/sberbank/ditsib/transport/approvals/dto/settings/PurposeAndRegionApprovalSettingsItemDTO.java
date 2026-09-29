package ru.sberbank.ditsib.transport.approvals.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.approvals.dto.TripPurposeDTO;

/**
 * DTO элемент настройки согласований заявок по цели и региону
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Элемент настройки согласований заявок по цели и региону")
public class PurposeAndRegionApprovalSettingsItemDTO {
    
    /** Цель поездки */
    @Schema(description = "Цель поездки")
    private TripPurposeDTO tripPurpose;
    
    /** Геозона. Если не указана, то настройка распространяется на все регионы */
    @Schema(description = "Геозона")
    private GeoZoneDTO region;
    
    /** Сумма, не требующая согласования */
    @Builder.Default
    @Schema(description = "Сумма, не требующая согласования, коп.")
    private long minCostToBeApproved = 0;
}
