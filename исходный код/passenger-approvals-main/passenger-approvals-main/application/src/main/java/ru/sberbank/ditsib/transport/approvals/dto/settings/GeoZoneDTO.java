package ru.sberbank.ditsib.transport.approvals.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO для геозоны
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Геозона", description = "Данные геозоны")
public class GeoZoneDTO {
    
    /** ID геозоны */
    @NotNull
    @Schema(description = "ID геозоны", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /** Наименование геозоны */
    @Schema(description = "Наименование геозоны")
    private String name;
    
    /** Код геозоны */
    @Schema(description = "Код геозоны")
    private Long code;
    
    /** ID родительской геозоны */
    @Schema(description = "ID родительской геозоны")
    private UUID parentId;
}
