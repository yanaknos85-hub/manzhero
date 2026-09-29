package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.limits.constants.ImportStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Object with data of client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportResultDTO {
    
    /**
     * Result of upload
     */
    @Schema(description = "Результат загрузки")
    ImportStatus resultStatus = ImportStatus.FAIL;
    
    /**
     * inserted items
     */
    @Schema(description = "Количество вставок")
    private long inserted = 0L;

    /**
     * updated items
     */
    @Schema(description = "Количество обновлений")
    private long updated = 0L;
    
    /**
     * deleted items
     */
    @Schema(description = "Количество удаленных")
    private long deleted = 0L;
    
    /**
     * unchanged items
     */
    @Schema(description = "Количество неизменных")
    private long unchanged = 0L;
    
    /**
     * Total bad items
     */
    @Schema(description = "Записей некорректных")
    private long errors = 0;
    
    /**
     * total processed
     */
    @Schema(description = "Всего записей до начала загрузки")
    private long total = 0L;
    
    
    @Schema(description = "Ошибки")
    private List<String> errorDescrs = new ArrayList<>();

}
