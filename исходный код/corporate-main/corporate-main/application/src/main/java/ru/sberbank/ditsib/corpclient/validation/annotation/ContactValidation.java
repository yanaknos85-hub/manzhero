package ru.sberbank.ditsib.corpclient.validation.annotation;

import ru.sberbank.ditsib.corpclient.validation.ContactValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Проверка корректности контакта.
 */
@Documented
@Constraint(validatedBy = ContactValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ContactValidation {
    
    String message() default "Contact not corresponding format";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
