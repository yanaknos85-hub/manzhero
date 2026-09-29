package ru.sber.transport.tariff_fleet.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Sort;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract_;
import ru.sber.transport.tariff_fleet.dto.AbstractSearchContractAllOrganizationsDto;

import java.util.Optional;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@ToString
@AllArgsConstructor
@ContractType(DocumentType.FUEL)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FuelSearchContractAllOrganizationsDto", description = "Договор Заправка топливом поиск (все организации)")
public class FuelSearchContractAllOrganizationsDto extends AbstractSearchContractAllOrganizationsDto {
    
    @Schema(description = "Идентификатор контрагента",
            requiredMode = NOT_REQUIRED,
            type = "string",
            format = "uuid",
            example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
    private final UUID contractorId;
    @Schema(description = "Идентификатор организации корпоративного клиента",
            requiredMode = NOT_REQUIRED,
            type = "string",
            format = "uuid",
            example = "6abbca61-7b1b-44c9-8c22-4df7ccee48b5")
    private final UUID organizationId;
    
    @Valid
    @Schema(description = "Настройки сортировки")
    private final FuelContractSortSetting sortSetting;
    
    @Override
    protected Sort getSort() {
        return Optional.ofNullable(sortSetting)
                       .map(sorting -> {
                           Sort sort = Sort.by(sortSetting.property().getFieldName());
                           return sortSetting.directionAsc() ? sort.ascending() : sort.descending();
                       })
                       .orElseGet(() -> Sort.by(Contract_.NUMBER).descending());
    }
}
