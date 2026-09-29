package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Data transfer object with data about position.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткие данные  должности", description = "Данные должности")
public class PositionShortDTO {
    /**
     * Identifier
     */
    @NotBlank
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Identifier (human readable)
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    /**
     * Position name
     */
    @Schema(description = "Название")
    @JsonProperty("positionName")
    private String name;
}
