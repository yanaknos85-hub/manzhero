package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostDto;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePostDto;

import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.EWB)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "EwbContractPostDto", description = "Договор ЭПЛ (создание)")
public class EwbContractPostDto extends AbstractContractPostDto {
    @NotNull
    @Schema(description = "Идентификатор записи об организации контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private final UUID contractorOrganizationId;
    @Schema(description = "Вид осмотра", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private final InspectionType inspectionType;
    @Schema(description = "Сумма договора (без НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99999999999",
            example = "344444")
    @NotNull
    @Max(value = 99999999999L)
    @PositiveOrZero
    private final Long amount;
    @Schema(description = "Идентификатор оператора ЭДО",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2BM")
    @NotBlank
    private final String edfOperatorId;
    @Schema(description = "Код участника",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1", maximum = "50",
            example = "001b5a570a8f4e7b")
    @NotBlank
    @Size(min = 1, max = 50)
    private final String edfCode;
    @Schema(description = "Медицинская лицензия организации контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private final OrganizationMedicalLicensePostDto contractorMedicalLicense;
}