package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * Объект с данными для запроса результата.
 */
@Builder
@Getter
@Schema(title = "Информация о задаче", description = "Информация о запущенной задаче")
public class TaskStartedDto {
    
    /**
     * URL запроса результата.
     */
    @JsonProperty("result_url")
    @Schema(description = "Путь запроса результатов")
    private final String url;
    
    /**
     * Флаг о том, что выгрузка запущена.
     */
    @Schema(description = "Выгрузка запущена")
    @Builder.Default
    private final boolean started = true;
    
}
