package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPatchDto;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePatchDto;

@AllArgsConstructor
@ContractType(DocumentType.EWB)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "EwbContractPatchDto", description = "Договор ЭПЛ (изменение)")
public class EwbContractPatchDto extends AbstractContractPatchDto {
    @Schema(description = "Идентификатор оператора ЭДО",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2BM")
    private final String edfOperatorId;
    @Schema(description = "Код участника",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minimum = "1", maximum = "50",
            example = "001b5a570a8f4e7b")
    @Size(min = 1, max = 50)
    private final String edfCode;
    @Schema(description = "Медицинская лицензия организации контрагента", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Valid
    private final OrganizationMedicalLicensePatchDto contractorMedicalLicense;
}