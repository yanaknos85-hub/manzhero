package ru.sberbank.ditsib.corpclient.config.converters;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.dto.OrganizationField;

/**
 * Конвертер перечисления полей организаций.
 */
@Component
public class OrganizationFieldEnumConverter extends SortEnumConverter<OrganizationField> {
    
    public OrganizationFieldEnumConverter() {
        super(OrganizationField.class);
    }
}
