package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.dto.AbstractGetContractByIdDto;
import ru.sber.transport.tariff_fleet.dto.DateRange;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(name = "EwbGetContractByIdDto", description = "Договор ЭПЛ (изменение)")
public class EwbGetContractByIdDto extends AbstractGetContractByIdDto {
    
    @NotBlank
    @Schema(description = "Наименование контрагента",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            example = "Мед Эксп")
    private final String contractorName;
    
    @NotNull
    @Schema(description = "Сумма договора (без НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "100000")
    private final Long amount;
    
    @NotNull
    @Schema(description = "Вид осмотра",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "MEDIC")
    private final InspectionType inspectionType;
    
    @NotBlank
    @Schema(description = "Идентификатор оператора ЭДО",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2BM")
    private final String edfOperatorId;
    
    @NotBlank
    @Schema(description = "Код участника",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "123123123")
    private final String edfCode;
    
    @NotNull
    @Schema(description = "Медицинская лицензия контрагента",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private final ContractorMedicalLicense contractorMedicalLicense;
    
    @Schema(description = "Медицинская лицензия контрагента")
    record ContractorMedicalLicense(
            @Schema(description = "Серия",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "123")
            String series,
            @Schema(description = "Номер",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "123123")
            String number,
            @Schema(description = "Дата выдачи",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "2023-01-01")
            LocalDate issueDate,
            @Schema(description = "Дата окончания срока действия",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "2023-01-02")
            LocalDate expiryDate
    ) {
    }
    
    public EwbGetContractByIdDto(
            @NotNull UUID id,
            @NotBlank @Size(min = 1, max = 50) String number,
            @NotBlank String uvhd,
            @NotNull LocalDate start, @NotNull LocalDate end,
            @NotBlank String contractorName,
            @NotNull @Min(value = 1L) @Max(value = 99999999999L) Long amount,
            @NotNull InspectionType inspectionType,
            @NotBlank String edfOperatorId,
            @NotBlank String edfCode,
            @NotBlank String medicalLicenseSeries,
            @NotBlank String medicalLicenseNumber,
            @NotNull LocalDate medicalLicenseIssueDate,
            @NotNull LocalDate medicalLicenseExpiryDate
                                ) {
        super(id, number, uvhd, new DateRange(start, end));
        this.contractorName = contractorName;
        this.amount = amount;
        this.inspectionType = inspectionType;
        this.edfOperatorId = edfOperatorId;
        this.edfCode = edfCode;
        this.contractorMedicalLicense =
                new ContractorMedicalLicense(medicalLicenseSeries, medicalLicenseNumber, medicalLicenseIssueDate, medicalLicenseExpiryDate);
    }
}
