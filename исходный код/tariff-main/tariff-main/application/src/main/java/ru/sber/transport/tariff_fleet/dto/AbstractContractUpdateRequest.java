package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractUpdateDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractUpdateDto;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RepairContractUpdateDto.class, name = "REPAIR_AND_MAINTENANCE"),
        @JsonSubTypes.Type(value = FuelContractUpdateDto.class, name = "FUEL")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Абстрактная модель запроса обновления контракта", description = "Абстрактная модель запроса обновления контракта")
public abstract class AbstractContractUpdateRequest {
    @Schema(description = "Тип документа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Сумма контракта (с НДС)",
            type = "long",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "1000000",
            minimum = "0",
            maximum = "99999999999")
    @Nullable
    private Long amountWithVat;
    @Schema(description = "Сумма контракта (без НДС)",
            type = "long",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "1000000",
            minimum = "0",
            maximum = "99999999999")
    @Nullable
    private Long amountWithoutVat;
    @Schema(description = "Логотип",
            type = "string",
            format = "binary",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    private String logo;

}

