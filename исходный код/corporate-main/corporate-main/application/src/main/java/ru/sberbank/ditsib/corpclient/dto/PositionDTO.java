package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


/**
 * Data transfer object with data about position.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Данные подразделения", description = "Данные подразделения")
public class PositionDTO extends NewPositionDTO {
    
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    @Schema(description = "Флаг активности")
    private boolean active;

    /**
     * Identifier (human readable)
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
}
