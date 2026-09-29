package ru.sber.transport.tariff_fleet.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.AbstractContractUpdateRequest;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;

import java.util.List;

@AllArgsConstructor
@ContractType(DocumentType.FUEL)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Schema(title = "Обновление договора заправка топливом", description = "Обновление договора заправка топливом")
public final class FuelContractUpdateDto extends AbstractContractUpdateRequest {

    @Schema(description = "Заправки",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    private final List<ServicePointDto> servicePoints;
    @Schema(description = "Имя для точек заправок",
            type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ул. Красная д.5",
            maxLength = 255,
            minLength = 1)
    @Size(min = 1, max = 255)
    private String name;
}
