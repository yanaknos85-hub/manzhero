package ru.sber.transport.tariff_fleet.dto.repair;

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
import ru.sber.transport.tariff_fleet.database.model.RepairContract_;
import ru.sber.transport.tariff_fleet.dto.AbstractSearchContractDto;

import java.util.Optional;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Getter
@ToString
@AllArgsConstructor
@ContractType(DocumentType.REPAIR_AND_MAINTENANCE)
@EqualsAndHashCode(callSuper = true)
public class RepairSearchContractDto extends AbstractSearchContractDto {
    
    @Schema(description = "Идентификатор контрагента",
            requiredMode = NOT_REQUIRED,
            type = "string",
            format = "uuid",
            example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
    private final UUID contractorId;
    
    @Valid
    @Schema(description = "Настройки сортировки")
    private final RepairContractSortSetting sortSetting;
    
    @Override
    protected Sort getSort(AbstractSearchContractDto searchDto) {
        return Optional.ofNullable(sortSetting)
                       .map(sorting -> {
                           Sort sort;
                           if (sortSetting.property() == RepairContractSortSetting.RepairContractSortOption.CONTRACTOR_NAME) {
                               sort = Sort.by(RepairContract_.SERVICE_POINTS_NAME);
                           } else {
                               sort = Sort.by(Contract_.NUMBER);
                           }
                           return sortSetting.directionAsc() ? sort.ascending() : sort.descending();
                       })
                       .orElseGet(() -> Sort.by(Contract_.NUMBER).descending());
    }
}
