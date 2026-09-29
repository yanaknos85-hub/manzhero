package ru.sber.transport.tariff_fleet.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.dto.AbstractContractGetDto;

import java.time.LocalDate;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
public class RepairContractGetDto extends AbstractContractGetDto {
    @NotNull
    @Schema(description = "Идентификатор записи контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private final UUID contractorId;
    @NotBlank
    @Schema(description = "Наименование контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String contractorName;
    @NotBlank
    @Schema(description = "Номер договора УВХД",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 50)
    private final String uvhd;
    @Schema(description = "Сумма договора (без НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1", maximum = "99999999999",
            example = "344444")
    @NotNull
    @Min(value = 1L)
    @Max(value = 99999999999L)
    @Positive
    private final Long amount;
    
    public RepairContractGetDto(
            @NotNull UUID id,
            @NotBlank @Size(min = 1, max = 50) String number,
            @NotNull LocalDate start,
            @NotNull LocalDate end,
            boolean active,
            @NotNull UUID contractorId,
            @NotBlank String contractorName, String uvhd,
            @NotNull @Min(value = 1L) @Max(value = 99999999999L) Long amount
                               ) {
        super(id, number, start, end, active);
        this.contractorId = contractorId;
        this.contractorName = contractorName;
        this.uvhd = uvhd;
        this.amount = amount;
    }
}
