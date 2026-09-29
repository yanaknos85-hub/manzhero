package ru.sberbank.ditsib.transport.limits.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Исключение, выбрасваемое при ошибках формирования файлов.
 */
@ResponseStatus(value = HttpStatus.EXPECTATION_FAILED, reason = "Ошибка формирования файла")
public class ExportException extends RuntimeException {
    
    /**
     * Создать исключение.
     *
     * @param error текст ошибки.
     */
    public ExportException(List<String> error) {
        super(String.join("\n", error));
    }
    
}
