package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.Optional;


@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Unexpected document type")
public class UnexpectedDocumentTypeValidationException extends BusinessException {
    
    public static final String DOCUMENT_TYP_MSG_FORMAT = "Невалидный тип документа в запросе, documentType:'%s'";
    public static final String CONTRACT_TYPE_MSG_FORMAT = "Неподдерживаемый операцией тип договора: %s";
    
    /**
     * Исключение для валидации типа документа
     *
     * @param documentType
     */
    public UnexpectedDocumentTypeValidationException(String messageFormat, DocumentType documentType) {
        super(String.format(messageFormat, Optional.ofNullable(documentType).map(Enum::name).orElse("N/A")));
    }
}