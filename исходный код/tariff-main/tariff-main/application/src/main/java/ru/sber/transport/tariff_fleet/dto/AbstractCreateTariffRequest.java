package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.fuel.CreateFuelTariffDto;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateRepairTariffDto.class, name = "REPAIR_AND_MAINTENANCE"),
        @JsonSubTypes.Type(value = CreateFuelTariffDto.class, name = "FUEL")
})
@Getter
@Schema(name = "AbstractContractPostAllOrganizationsDto", description = "Абстрактный запрос на создание тарифа")
public abstract class AbstractCreateTariffRequest {
    @Schema(description = "Тип услуги", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;

    @NotNull
    @Schema(description = "ID договора", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractId;


}
