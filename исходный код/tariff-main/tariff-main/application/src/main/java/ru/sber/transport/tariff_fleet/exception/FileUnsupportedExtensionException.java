package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File upload error")
public class FileUnsupportedExtensionException extends BusinessException {
    
    public static final String FILE_UPLOAD_ERROR = "Ожидается файл формата %s";
    
    public FileUnsupportedExtensionException(String supportedFormats) {
        super(String.format(FILE_UPLOAD_ERROR, supportedFormats));
    }
    
    
}
