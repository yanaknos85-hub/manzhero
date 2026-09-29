package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbSearchContractDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairSearchContractDto;

/**
 * Абстрактный запрос на поиск договоров
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EwbSearchContractDto.class, name = "EWB"),
        @JsonSubTypes.Type(value = RepairSearchContractDto.class, name = "REPAIR_AND_MAINTENANCE")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractSearchContractDto", description = "Абстрактный запрос на поиск договоров")
public abstract class AbstractSearchContractDto {
    @Schema(description = "Тип документа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Номер договора",
            minimum = "3", maximum = "50",
            example = "4387345-1")
    @Size(min = 3, max = 50)
    private String number;
    @Schema(description = "Флаг активности")
    private Boolean active;
    @Schema(description = "Период действия договора")
    @Valid
    @Setter
    private DateRange period;
    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;
    
    public PageRequest getPageRequest() {
        return getPageRequest(getSort(this));
    }
    
    private PageRequest getPageRequest(@NotNull Sort sort) {
        if (this.getPageSetting() == null) {
            return PageRequest.of(0, 10, sort);
        }
        return PageRequest.of(this.getPageSetting().page(), this.getPageSetting().size(), sort);
    }
    
    protected abstract Sort getSort(AbstractSearchContractDto searchDto);
}
