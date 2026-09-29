package ru.sber.transport.tariff_fleet.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Sort;
import ru.sber.transport.tariff_fleet.annotation.ContractType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.model.Tariff_;
import ru.sber.transport.tariff_fleet.dto.AbstractSearchTariffDto;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@ContractType(DocumentType.EWB)
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@Schema(name = "EwbSearchTariffDto", description = "Запрос на поиск тарифов ЭПЛ")
public class EwbSearchTariffDto extends AbstractSearchTariffDto {
    @Schema(description = "Идентификатор записи об организации")
    private final UUID organizationId;
    @Schema(description = "Идентификатор записи об подразделении")
    private final UUID departmentId;
    @Schema(description = "Идентификатор записи об организации договора")
    private final UUID contractOrganizationId;
    @Schema(description = "Вид осмотра")
    private final InspectionType inspectionType;
    @Schema(description = "Настройки сортировки")
    @Valid
    private final EwbTariffSortSetting sortSetting;
    
    @Override
    protected Sort getSort(AbstractSearchTariffDto searchDto) {
        return Optional.ofNullable(sortSetting)
                       .map(sorting -> {
                           var sort = Sort.by(Tariff_.HUMAN_READABLE_ID);
                           return sortSetting.directionAsc() ? sort.ascending() : sort.descending();
                       })
                       .orElseGet(() -> Sort.by(Tariff_.HUMAN_READABLE_ID).descending());
    }
}
