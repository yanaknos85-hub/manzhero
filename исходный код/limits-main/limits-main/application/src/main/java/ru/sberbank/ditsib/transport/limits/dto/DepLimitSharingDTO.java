package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Распределение на подразделение",
        description = "Распределение на подразделение")
public class DepLimitSharingDTO {
    
    /**
     * Sum.
     */
    @Schema(description = "Суммы по типам транспорта")
    private List<DepLimitSharingPerTransportDTO> sharingPerTransportList;
    
    /**
     * Target department id.
     */
    @Schema(description = "Целевое подразделение")
    private UUID targetDepartmentId;
    
    /**
     * Final sharing flag
     */
    @Schema(description = "Конечное распределение")
    private Boolean finalSharing;
}
