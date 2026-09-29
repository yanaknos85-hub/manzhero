package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Информация о должности для настроек совместных поездок", description = "Данные о должности")
@Builder
public class SettingsPositionDTO {
    
    /**
     * Идентификатор должности
     */
    @Schema(description = "Идентификатор должности")
    @NotNull
    private UUID id;
    
    /**
     * Наименование должности
     */
    @Schema(description = "Наименование должности")
    @NotBlank
    @JsonPropertyOrder("positionName")
    private String name;
}
