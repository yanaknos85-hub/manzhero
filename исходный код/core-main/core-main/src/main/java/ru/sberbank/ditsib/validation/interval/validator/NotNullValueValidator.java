package ru.sberbank.ditsib.validation.interval.validator;

import lombok.SneakyThrows;
import ru.sberbank.ditsib.validation.interval.NotNullValue;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Валидатор ненулевых значений.
 */
public class NotNullValueValidator implements ConstraintValidator<NotNullValue, Object> {
    private String[] fieldArray;

    @Override
    public void initialize(NotNullValue constraint) {
        fieldArray = constraint.fields();
    }

    @SneakyThrows
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        var validatableItemClass = object.getClass();

        var allNull = true;
        for (var fieldName : fieldArray) {
            var field = getField(validatableItemClass, fieldName);
            field.setAccessible(true); // NOSONAR needs reflective access
            allNull = allNull && field.get(object) == null;
        }
        return !allNull;
    }

    private Field getField(Class<?> itemClass, String fieldName) {
        var fieldsStream = Stream.of(itemClass.getFields());
        var declaredFieldsStream = Stream.of(itemClass.getDeclaredFields());
        return Stream.concat(fieldsStream, declaredFieldsStream)
                .filter(check -> check.getName().equals(fieldName)).findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Field with name '%s' not found", fieldName)));
    }
}
