package ru.sber.transport.tariff_fleet.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff_fleet.exception.InvalidDocumentType;

/**
 * Тип документа
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "Тип документа", description = "Тип документа")
public enum DocumentType {
    @Schema(description = "ЭПЛ")
    EWB("ЭПЛ"),
    @Schema(description = "Электрозаправка")
    ELECTRIC_FUEL("Электрозаправка"),
    @Schema(description = "Ремонт и ТО")
    REPAIR_AND_MAINTENANCE("Ремонт и ТО"),
    @Schema(description = "Заправка топливом")
    FUEL("Заправка топливом");

    private final String local;
    
    public static DocumentType resolveDocumentType(String documentType) {
        try {
            return DocumentType.valueOf(documentType);
        } catch (IllegalArgumentException e) {
            throw new InvalidDocumentType(documentType);
        }
    }
}