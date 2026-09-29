package ru.sber.transport.tariff_fleet.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.FUEL)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "FuelContractPostAllOrganizationsDto", description = "Договор Заправка топливом создание (все организации)")
public class FuelContractPostAllOrganizationsDto extends AbstractContractPostAllOrganizationsDto {
    @NotNull
    @Schema(description = "Идентификатор записи об контрагенте",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "f173f387-f283-466c-9124-89acc1678ac3")
    private final UUID contractorId;
    
    @NotNull
    @Schema(description = "Заказчик/корп. клиент",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "371890e0-c321-4bfd-b517-ab0189ea5156")
    private final UUID organizationId;
    
    @NotNull
    @Max(value = 99999999999L)
    @PositiveOrZero
    @Schema(description = "Сумма контракта (с НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99999999999",
            example = "344444")
    private final Long amountWithVat;
    
    @NotNull
    @Max(value = 99999999999L)
    @PositiveOrZero
    @Schema(description = "Сумма контракта (без НДС)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99999999999",
            example = "344333")
    private final Long amountWithoutVat;
    
    @NotEmpty
    @Pattern(regexp = "^[^\\s:*?\"<>+|][^:*?\"<>+|]{0,48}[^\\s\\\\.]$|^[^\\s:*?\"<>+|.]$",
             message = "Название должно быть от 1 до 50 символов. Специальные символы запрещены. В начале и конце не должно быть пробелов. " +
                       "Точка в конце не допускается.")
    @Schema(description = "Наименование сервиса",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Автосервис",
            pattern = "^[^\\s:*?\"<>+|][^:*?\"<>+|]{0,48}[^\\s\\\\.]$|^[^\\s:*?\"<>+|.]$")
    private final String name;
    
    @Schema(description = "Логотип",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private final String logo;
    
    @Schema(hidden = true)
    private final UUID logoS3Id;
    
    @Schema(description = "Заправки",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private final List<ServicePointDto> servicePoints;
}