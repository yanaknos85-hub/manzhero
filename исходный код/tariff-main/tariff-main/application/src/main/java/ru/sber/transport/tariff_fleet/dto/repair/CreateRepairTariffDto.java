package ru.sber.transport.tariff_fleet.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractCreateTariffRequest;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.REPAIR_AND_MAINTENANCE)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "CreateRepairTariffDto", description = "Абстрактный запрос на создание тарифа для договора ремонта")
public class CreateRepairTariffDto extends AbstractCreateTariffRequest {
    @Schema(description = "Выездной сервис",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private boolean isFieldService;
    @Schema(description = "Стоимость нормо-часа, руб",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "999999")
    @NotNull
    @Min(0)
    @Max(999999)
    private int hourNormalizedPrice;
    @Schema(description = "Размер скидки на запасные части, %",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "100")
    @NotNull
    @Min(0)
    @Max(100)
    private BigDecimal detailDiscountPrice;
    @Schema(description = "Гарантия на работы по времени, месяцы",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99")
    @NotNull
    @Min(0)
    @Max(99)
    private int workWarranty;
    @Schema(description = "Гарантия на работы по пробегу, км",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99999")
    @NotNull
    @Min(0)
    @Max(99999)
    private int mileageWarranty;
    @Schema(description = "Гарантия на запчасти, месяцы",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99")
    @NotNull
    @Min(0)
    @Max(99)
    private int detailWarranty;
    @NotNull
    private UUID departmentId;
}
