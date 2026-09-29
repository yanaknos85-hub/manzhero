package ru.sberbank.ditsib.transport.limits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Поддерживаемые типы данных.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "Типы данных", description = "Типы данных, доступные для экспорта/импорта")
public enum FileType {
    /**
     * XLSX (Office  c 2007 и выше).
     */
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

    /**
     * XLS (Office до 2007).
     */
    XLS("application/vnd.ms-excel"),
    
    /**
     * CSV (Comma Separated Values).
     */
    CSV("text/csv"),
    
    /**
     * TXT (Text).
     */
    TXT("text/plain");
    
    /**
     * MIME-тип.
     */
    private final String mime;
    
}
