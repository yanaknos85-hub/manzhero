package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.model.Contract_;
import ru.sber.transport.tariff_fleet.dto.AbstractSearchContractDto;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.EWB)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "EwbSearchContractDto", description = "Запрос на поиск договоров ЭПЛ")
public class EwbSearchContractDto extends AbstractSearchContractDto {
    @Schema(description = "Идентификатор записи об организации контрагента",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            type = "string",
            format = "uuid",
            example = "e2629bd9-d5b6-4182-bcfc-d31fcddceb95")
    private final UUID contractorOrganizationId;
    @Schema(description = "Вид осмотра",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "MEDIC")
    private final InspectionType inspectionType;
    @Schema(description = "Настройки сортировки")
    @Valid
    private final EwbContractSortSetting sortSetting;
    
    @Override
    protected Sort getSort(AbstractSearchContractDto searchDto) {
        return Optional.ofNullable(sortSetting)
                       .map(sorting -> {
                           Sort sort;
                           if (sortSetting.property() == EwbContractSortSetting.EwbContractSortOption.CONTRACTOR_ORGANIZATION_NAME) {
                               sort = JpaSort.unsafe("(organizationName)");
                           } else {
                               sort = Sort.by(Contract_.NUMBER);
                           }
                           return sortSetting.directionAsc() ? sort.ascending() : sort.descending();
                       })
                       .orElseGet(() -> Sort.by(Contract_.NUMBER).descending());
    }
}
