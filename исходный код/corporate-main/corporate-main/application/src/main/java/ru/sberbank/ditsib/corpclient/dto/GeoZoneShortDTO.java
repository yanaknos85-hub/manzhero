package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Объект обмена данными о гео зоне.
 */
@Getter
@Setter
@ToString
@Schema(title = "Краткая информация о гео зоне", description = "Данные гео зоны")
public class GeoZoneShortDTO {

    /**
     * Идентификатор гео зоны.
     */
    @NotBlank
    @Schema(description = "Идентификатор гео зоны")
    private UUID id;

    /**
     * Наименование гео зоны.
     */
    @NotBlank
    @Schema(description = "Наименование гео зоны")
    private String name;
}
