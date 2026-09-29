package ru.sber.transport.tariff_fleet.dto.ewb;

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
public class EwbContractGetDto extends AbstractContractGetDto {
    @NotBlank
    @Schema(description = "Наименование организации контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String contractorOrganizationName;
    @Schema(description = "Сумма договора (без НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1", maximum = "99999999999",
            example = "344444")
    @NotNull
    @Min(value = 1L)
    @Max(value = 99999999999L)
    @Positive
    private final Long amount;
    @Schema(description = "Вид осмотра", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private final String inspectionType;
    
    @SuppressWarnings("java:S107")
    public EwbContractGetDto(
            @NotNull UUID id, @NotBlank @Size(min = 1, max = 50) String number, @NotNull LocalDate start, @NotNull LocalDate end,
            boolean active, @NotBlank String organizationName,
            @NotNull @Min(value = 1L) @Max(value = 99999999999L) Long amount, @NotNull String inspectionType
                            ) {
        super(id, number, start, end, active);
        this.contractorOrganizationName = organizationName;
        this.amount = amount;
        this.inspectionType = inspectionType;
    }
}
