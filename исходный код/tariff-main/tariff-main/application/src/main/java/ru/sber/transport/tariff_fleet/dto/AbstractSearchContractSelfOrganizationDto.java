package ru.sber.transport.tariff_fleet.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelSearchContractSelfOrganizationDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairSearchContractSelfOrganizationDto;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;

/**
 * Абстрактный запрос на поиск договоров
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "documentType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RepairSearchContractSelfOrganizationDto.class, name = "REPAIR_AND_MAINTENANCE"),
        @JsonSubTypes.Type(value = FuelSearchContractSelfOrganizationDto.class, name = "FUEL")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AbstractSearchContractSelfOrganizationDto", description = "Абстрактный запрос на поиск договоров (своя организация)")
public abstract class AbstractSearchContractSelfOrganizationDto {
    @Schema(description = "Тип документа",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "FUEL")
    @NotNull
    private DocumentType documentType;
    @Schema(description = "Номер договора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minimum = "3", maximum = "50",
            example = "4387345-1")
    @Size(min = 3, max = 50)
    private String number;
    @Schema(description = "Флаг активности",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;
    @Schema(description = "Начало периода",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2023-11-16")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate start;
    @Schema(description = "Окончание периода",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2023-11-16")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate end;
    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;
    
    public PageRequest getPageRequest() {
        return getPageRequest(getSort());
    }
    
    private PageRequest getPageRequest(@NotNull Sort sort) {
        if (this.getPageSetting() == null) {
            return PageRequest.of(0, 10, sort);
        }
        return PageRequest.of(this.getPageSetting().page(), this.getPageSetting().size(), sort);
    }
    
    protected abstract Sort getSort();
}
