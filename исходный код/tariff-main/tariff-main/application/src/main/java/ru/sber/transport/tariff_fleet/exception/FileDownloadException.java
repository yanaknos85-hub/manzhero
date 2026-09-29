package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File download error")
public class FileDownloadException extends BusinessException {

    public static final String FILE_UPLOAD_ERROR = "Ошибка при выгрузке файла: %s";

    public FileDownloadException(String storageId) {
        super(FILE_UPLOAD_ERROR.formatted(storageId));
    }
}
