package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

/**
 * Информация об изменяемом элементе настройки совместных поездок. Содержит ID
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(
        title = "Информация об изменяемом элементе настройки совместных поездок",
        description = "Информация об изменяемом элементе настройки совместных поездок"
)
public class SharedRideSettingsItemUpdateDTO extends SharedRideSettingsItemCreateDTO {
    
    /**
     * Идентификатор настройки
     */
    @Schema(description = "Идентификатор настройки")
    @NotNull
    private UUID id;
    
    @Builder(builderMethodName = "childBuilder")
    public SharedRideSettingsItemUpdateDTO(
            Set<@Valid SettingsPositionDTO> positions,
            Set<@Valid SettingsAttributeDTO> attributes,
            Set<@Valid SettingsEmployeeDTO> employees,
            @NotNull UUID id
    ) {
        super(positions, attributes, employees);
        this.id = id;
    }
}
