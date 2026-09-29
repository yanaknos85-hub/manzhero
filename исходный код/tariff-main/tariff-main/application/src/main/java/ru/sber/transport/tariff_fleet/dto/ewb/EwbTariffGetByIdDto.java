package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffGetByIdDto;

import java.util.UUID;

@Getter
@ToString
@Schema(name = "EwbTariffGetByIdDto", description = "Тариф ЭПЛ (получение по ID)")
public class EwbTariffGetByIdDto extends AbstractTariffGetByIdDto {
    
    @NotNull
    @Schema(description = "Вид осмотра",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "MEDIC")
    private final String inspectionType;
    @NotNull
    @Schema(description = "Владелец автопарка", requiredMode = Schema.RequiredMode.REQUIRED)
    private final FleetOwner fleetOwner;
    @NotNull
    @Schema(description = "Стоимость осмотра",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "integer",
            example = "1054")
    private final Long amount;
    
    @NotBlank
    @Schema(description = "Наименование контрагента",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            example = "Мед Эксп",
            minLength = 1, maxLength = 255)
    private final String contractorName;
    
    
    public EwbTariffGetByIdDto(
            @NotNull UUID id,
            @NotNull String contractNumber,
            @NotNull String inspectionType,
            @NotNull String fleetOwnerName,
            @NotNull String fleetOwnerDepartmentName,
            @NotNull Long amount,
            @NotNull String contractorName
                              ) {
        super(id, contractNumber);
        this.inspectionType = inspectionType;
        this.fleetOwner = new FleetOwner(fleetOwnerName, fleetOwnerDepartmentName);
        this.amount = amount;
        this.contractorName = contractorName;
    }
    
    @Schema(description = "Владелец автопарка")
    public record FleetOwner(
            @NotBlank
            @Schema(description = "Наименование",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    example = "Супер Автопарк",
                    minLength = 1, maxLength = 255)
            String name,
            @NotBlank
            @Schema(description = "Наименование  Подразделения",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    example = "КИЦ",
                    minLength = 1, maxLength = 255)
            String departmentName
    ) {
    }
}
