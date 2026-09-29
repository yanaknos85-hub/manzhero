package ru.sberbank.ditsib.validation.interval;

import ru.sberbank.ditsib.validation.interval.validator.NotNullValueValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Проверка полей на отсутствие значения.
 */
@Constraint(validatedBy = NotNullValueValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNullValue {

    /**
     * @return массив названий полей.
     */
    String[] fields();

    /**
     * Error message.
     *
     * @return message of interval error.
     */
    String message() default "Null values are not allowed for all fields";

    /**
     * Validation groups.
     *
     * @return groups.
     */
    Class<?>[] groups() default {};

    /**
     * Validation payload.
     *
     * @return payload.
     */
    Class<? extends Payload>[] payload() default {};
}
