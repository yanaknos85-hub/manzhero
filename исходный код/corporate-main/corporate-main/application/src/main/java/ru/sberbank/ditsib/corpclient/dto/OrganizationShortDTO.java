package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data transfer object with data about organization.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткие данные об организации", description = "Данные организации")
public class OrganizationShortDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Official name
     */
    @Schema(description = "Название")
    private String officialName;
}
