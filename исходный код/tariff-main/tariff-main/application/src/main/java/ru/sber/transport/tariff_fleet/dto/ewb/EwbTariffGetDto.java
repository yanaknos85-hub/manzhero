package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffGetDto;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
public class EwbTariffGetDto extends AbstractTariffGetDto {
    @Schema(description = "Вид осмотра", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private final String inspectionType;
    @NotBlank
    @Schema(description = "Наименование организации", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String organizationName;
    @NotBlank
    @Schema(description = "Наименование подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String departmentName;
    @NotBlank
    @Schema(description = "Наименование организации договора", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String contractOrganizationName;
    
    public EwbTariffGetDto(
            @NotNull UUID id, @NotBlank @Size(min = 1, max = 50) String contractNumber, @NotBlank @Size(max = 17) String humanReadableId, boolean active,
            @NotNull String inspectionType, @NotBlank String organizationName, @NotBlank String departmentName,
            @NotBlank String contractOrganizationName
                          ) {
        super(id, contractNumber, humanReadableId, active);
        this.inspectionType = inspectionType;
        this.organizationName = organizationName;
        this.departmentName = departmentName;
        this.contractOrganizationName = contractOrganizationName;
    }
}
