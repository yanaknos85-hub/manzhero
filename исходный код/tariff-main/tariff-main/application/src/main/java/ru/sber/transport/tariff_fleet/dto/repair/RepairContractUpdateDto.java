package ru.sber.transport.tariff_fleet.dto.repair;

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
@ContractType(DocumentType.REPAIR_AND_MAINTENANCE)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Schema(title = "Обновление договора на ремонт", description = "Обновление договора на ремонт")
public final class RepairContractUpdateDto extends AbstractContractUpdateRequest {
    @Schema(description = "Автосервисы",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    private final List<ServicePointDto> servicePoints;

    @Schema(description = "Имя для сервисных точек",
            type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ул. Красная д.5",
            maxLength = 255,
            minLength = 1)
    @Size(min = 1, max = 255)
    private String name;
}