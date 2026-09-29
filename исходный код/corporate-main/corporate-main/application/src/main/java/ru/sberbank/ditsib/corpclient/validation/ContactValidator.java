package ru.sberbank.ditsib.corpclient.validation;

import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.corpclient.dto.ContactDto;
import ru.sberbank.ditsib.corpclient.validation.annotation.ContactValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Валидатор значения контактов.
 */
@Slf4j
public class ContactValidator implements ConstraintValidator<ContactValidation, ContactDto> {
    
    @Override
    public boolean isValid(ContactDto contact, ConstraintValidatorContext constraintValidatorContext) {
        var type = contact.getType();
        var value = contact.getValue();
        return Pattern.matches(type.getRegexp(), value);
    }
}
