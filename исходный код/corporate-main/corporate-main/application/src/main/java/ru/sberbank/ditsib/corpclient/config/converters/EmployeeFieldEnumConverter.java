package ru.sberbank.ditsib.corpclient.config.converters;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.dto.EmployeeField;

/**
 * Конвертер перечисления полей сотрудников.
 */
@Component
public class EmployeeFieldEnumConverter extends SortEnumConverter<EmployeeField> {
    
    public EmployeeFieldEnumConverter() {
        super(EmployeeField.class);
    }
}
