package ru.sber.transport.tariff_fleet.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractContractGetSelfOrganizationDto;

import java.time.LocalDate;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
public class RepairContractGetSelfOrganizationDto extends AbstractContractGetSelfOrganizationDto {
    
    @NotBlank
    @Schema(description = "Наименование контрагента",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Сервис")
    private final String contractorName;
    @NotBlank
    @Schema(description = "Наименование организации корпоративного клиента",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Байкальский банк")
    private final String organizationName;
    @Schema(description = "Сумма договора (без НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1", maximum = "99999999999",
            example = "344444")
    @NotNull
    @Min(value = 1L)
    @Max(value = 99999999999L)
    @Positive
    private final Long amountWithoutVat;
    
    public RepairContractGetSelfOrganizationDto(
            @NotNull UUID id,
            @NotNull DocumentType documentType,
            @NotBlank @Size(min = 1, max = 50) String number,
            @NotNull LocalDate start,
            @NotNull LocalDate end,
            boolean active,
            @NotBlank String contractorName,
            @NotBlank String organizationName,
            @NotNull @Min(value = 1L) @Max(value = 99999999999L) Long amountWithoutVat
                                               ) {
        super(id, documentType, number, start, end, active);
        this.contractorName = contractorName;
        this.organizationName = organizationName;
        this.amountWithoutVat = amountWithoutVat;
    }
}
