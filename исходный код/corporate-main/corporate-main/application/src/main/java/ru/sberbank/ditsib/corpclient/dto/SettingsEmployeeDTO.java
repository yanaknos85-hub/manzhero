package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Информация о сотруднике для настроек совместных поездок
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Информация о сотруднике для настроек совместных поездок", description = "Данные сотрудника")
@Builder
public class SettingsEmployeeDTO {
    
    /**
     * Идентификатор сотрудника
     */
    @Schema(description = "Идентификатор")
    @NotNull
    private UUID id;
    
    /**
     * Имя сотрудника
     */
    @Schema(description = "Имя сотрудника")
    @NotBlank
    private String firstName;
    
    /**
     * Фамилия сотрудника
     */
    @Schema(description = "Фамилия сотрудника")
    @NotBlank
    private String lastName;
    
    /**
     * Отчество сотрудника
     */
    @Schema(description = "Отчество сотрудника")
    private String patronymic;
    
    /**
     * Табельный номер сотрудника
     */
    @Schema(description = "Табельный номер сотрудника")
    @NotBlank
    private String personnelNumber;
    
    /**
     * Идентификатор должности сотрудника
     */
    @Schema(description = "Идентификатор должности сотрудника")
    @NotNull
    private UUID positionId;
}
