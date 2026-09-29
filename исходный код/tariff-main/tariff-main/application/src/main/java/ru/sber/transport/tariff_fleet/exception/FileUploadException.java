package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File upload error")
public class FileUploadException extends BusinessException {
    
    public static final String FILE_UPLOAD_ERROR = "Ошибка при загрузке файла";
    public static final String S3_STORAGE_FILE_UPLOAD_ERROR = "Ошибка при загрузке файла в хранилище S3";
    
    public FileUploadException() {
        super(FILE_UPLOAD_ERROR);
    }
    
    public FileUploadException(String message) {
        super(message);
    }
}
