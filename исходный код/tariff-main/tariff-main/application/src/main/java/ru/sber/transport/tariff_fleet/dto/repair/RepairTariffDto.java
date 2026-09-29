package ru.sber.transport.tariff_fleet.dto.repair;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RepairTariffDto extends AbstractTariffResponse {
    @Schema(description = "Выездной сервис",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "isFieldService")
    private boolean isFieldService;
    @Schema(description = "Стоимость нормо-часа, руб",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "999999")
    private int hourNormalizedPrice;
    @Schema(description = "Размер скидки на запасные части, %",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "100")
    private BigDecimal detailDiscountPrice;
    @Schema(description = "Гарантия на работы по времени, месяцы",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99")
    private int workWarranty;
    @Schema(description = "Гарантия на работы по пробегу, км",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99999")
    private int mileageWarranty;
    @Schema(description = "Гарантия на запчасти, месяцы",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0", maximum = "99")
    private int detailWarranty;
    @Schema(description = "Наименование подразделения",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String departmentName;
}
