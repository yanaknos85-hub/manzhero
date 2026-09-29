package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.List;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unsupported document type")
public class UnsupportedDocumentTypeException extends BusinessException {
    public UnsupportedDocumentTypeException(DocumentType unsupportedDocumentType, List<DocumentType> supportedDocumentTypeList) {
        super(String.format("Данная операция не поддерживает тип %s. Доступные типы документов: %s", unsupportedDocumentType,
                            supportedDocumentTypeList));
    }
}
