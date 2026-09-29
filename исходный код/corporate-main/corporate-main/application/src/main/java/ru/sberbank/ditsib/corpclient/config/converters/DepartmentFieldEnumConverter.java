package ru.sberbank.ditsib.corpclient.config.converters;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.dto.DepartmentField;

/**
 * Конвертер перечисления полей подразделения.
 */
@Component
public class DepartmentFieldEnumConverter extends SortEnumConverter<DepartmentField> {
    
    public DepartmentFieldEnumConverter() {
        super(DepartmentField.class);
    }
}
