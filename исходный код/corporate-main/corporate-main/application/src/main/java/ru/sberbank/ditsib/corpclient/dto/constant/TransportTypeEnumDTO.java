package ru.sberbank.ditsib.corpclient.dto.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@Schema(title = "Тип транспорта", description = "Описание типа транспорта")
public class TransportTypeEnumDTO {
    /**
     * ID.
     */
    @Schema(description = "Идентификатор")
    private final UUID id;
    
    /**
     * Имя
     */
    @Schema(description = "Название")
    private final String name;
    
    /**
     * Русское описание
     */
    @Schema(description = "Русское описание для отображения")
    private final String rusName;
}
