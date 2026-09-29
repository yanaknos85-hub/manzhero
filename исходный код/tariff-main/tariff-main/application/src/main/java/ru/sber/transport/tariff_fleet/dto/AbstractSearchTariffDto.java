package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbSearchTariffDto;

/**
 * Абстрактный запрос на поиск тарифов
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EwbSearchTariffDto.class, name = "EWB")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractSearchTariffDto", description = "Абстрактный запрос на поиск тарифов")
public abstract class AbstractSearchTariffDto {
    @Schema(description = "Тип документа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Флаг активности")
    private Boolean active;
    @Schema(description = "Человекочитаемый идентификатор",
            minimum = "3", maximum = "17",
            example = "TF-0008")
    @Size(min = 3, max = 17)
    private String humanReadableId;
    @NotNull
    @Schema(description = "Настройки разделения на страницы", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageSetting pageSetting;
    
    public PageRequest getPageRequest(AbstractSearchTariffDto searchDto) {
        return getPageRequest(searchDto, getSort(searchDto));
    }
    
    private PageRequest getPageRequest(AbstractSearchTariffDto searchDto, @NotNull Sort sort) {
        if (searchDto.getPageSetting() == null) {
            return PageRequest.of(0, 10, sort);
        }
        return PageRequest.of(searchDto.getPageSetting().page(), searchDto.getPageSetting().size(), sort);
    }
    
    protected abstract Sort getSort(AbstractSearchTariffDto searchDto);
}
