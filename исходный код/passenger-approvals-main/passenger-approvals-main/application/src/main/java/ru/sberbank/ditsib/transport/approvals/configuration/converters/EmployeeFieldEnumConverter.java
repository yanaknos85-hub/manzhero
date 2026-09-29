package ru.sberbank.ditsib.transport.approvals.configuration.converters;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.approvals.configuration.SortEnumConverter;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeField;

@Component
public class EmployeeFieldEnumConverter extends SortEnumConverter<EmployeeField> {
    
    public EmployeeFieldEnumConverter() {
        super(EmployeeField.class);
    }
}
