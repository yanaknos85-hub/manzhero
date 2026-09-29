package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Информация о признаках сотрудника для настроек совместных поездок
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(
        title = "Информация о признаках сотрудника для настроек совместных поездок",
        description = "Данные о признаках сотрудника"
)
@Builder
public class SettingsAttributeDTO {
    
    /**
     * Идентификатор признака
     */
    @Schema(description = "Идентификатор признака")
    @NotNull
    private UUID id;
    
    /**
     * Признак сотрудника
     */
    @Schema(description = "Признак сотрудника")
    @NotBlank
    private String name;
}
