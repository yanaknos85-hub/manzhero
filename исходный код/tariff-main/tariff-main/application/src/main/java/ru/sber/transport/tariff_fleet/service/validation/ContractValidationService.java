package ru.sber.transport.tariff_fleet.service.validation;

import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.dto.DateRange;

import java.time.LocalDate;

public interface ContractValidationService {
    
    void validateUvhdUnique(String uvhd);
    
    void validateDateRange(LocalDate start, LocalDate end);
    
    LocalDate validateStartAndGet(DateRange period, Boolean active);
    
    LocalDate validateEndAndGet(DateRange period, Boolean active);
    
    void validateFindByDocumentType(DocumentType documentType);
}
