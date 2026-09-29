package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Лимит", description = "Данные лимита")
public class PositionSearchDTO {
    
    @Schema(description = "Организация")
    private UUID organizationId;
    
    @Schema(description = "Человекочитаемый идентификатор должности")
    private String humanReadableId;
    
    @Schema(description = "Название")
    private String positionName;
    
    @Schema(description = "Классы такси")
    @Builder.Default
    private Set<TaxiClass> availableClasses = new HashSet<>();
    
    @Schema(description = "Автосогласование")
    private Boolean selfApproved;
    
    @Schema(description = "Признак активности")
    @Builder.Default
    private boolean active = true;
    
}
