package ru.sberbank.ditsib.validation.interval.validator;

import lombok.SneakyThrows;
import ru.sberbank.ditsib.validation.interval.Interval;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static ru.sberbank.ditsib.validation.interval.Interval.Include.NONE;
import static ru.sberbank.ditsib.validation.interval.Interval.DateNullStrategy.STRICT;

/**
 * Валидатор интервалов.
 */
public class IntervalValidator implements ConstraintValidator<Interval, Object> {
    
    private String startFieldName;
    
    private String endFieldName;
    
    private Interval.Include inclusion;
    
    private Interval.DateNullStrategy startNullStrategy;
    
    private Interval.DateNullStrategy endNullStrategy;
    
    @Override
    public void initialize(Interval constraint) {
        startFieldName = constraint.startField();
        endFieldName = constraint.endField();
        inclusion = constraint.inclusion();
        startNullStrategy = constraint.startNullStrategy();
        endNullStrategy = constraint.endNullStrategy();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        var validatableItemClass = object.getClass();
        var startField = getField(validatableItemClass, startFieldName);
        var endField = getField(validatableItemClass, endFieldName);
        var startFieldAccessor = startField.canAccess(object);
        var endFieldAccessor = endField.canAccess(object);
        startField.setAccessible(true); // NOSONAR need reflective access
        endField.setAccessible(true); // NOSONAR need reflective access
        if (!matchValueTypes(startField, endField)) {
            return false;
        }
        var startValue = getValue(object, startField);
        var endValue = getValue(object, endField);
        if (startValue == null && endValue == null) {
            return true;
        }
        if (STRICT.equals(startNullStrategy)) {
            return startValue != null;
        }
        if (STRICT.equals(endNullStrategy)) {
            return endValue != null;
        }
        
        var compare = Objects.<Comparable>compare(startValue, endValue, Comparator.naturalOrder());
        var compareResult = NONE.equals(inclusion) ? compare < 0 : compare <= 0;
        startField.setAccessible(startFieldAccessor);
        endField.setAccessible(endFieldAccessor);
        return compareResult;
    }
    
    /**
     * Get value of field from object.
     *
     * @param object source object.
     * @param field field.
     *
     * @return comparable values.
     */
    @SneakyThrows({ IllegalArgumentException.class, IllegalAccessException.class })
    private Comparable<?> getValue(Object object, Field field) {
        var value = field.get(object);
        if (value == null) {
            return null;
        }
        if (Comparable.class.isAssignableFrom(value.getClass())) {
            return (Comparable<?>) value;
        }
        throw new IllegalArgumentException(String.format("Value of field '%s' must implement Comparable",
                                                         field.getName()));
    }
    
    /**
     * Match value types.
     *
     * @param startField start interval field.
     * @param endField end interval field.
     *
     * @return <code>true</code> if types are the same.
     */
    private boolean matchValueTypes(Field startField, Field endField) {
        return startField.getType().getCanonicalName().equals(endField.getType().getCanonicalName());
    }
    
    /**
     * Get field with name.
     *
     * @param itemClass validatable item class.
     * @param fieldName name of field.
     *
     * @return found field.
     */
    private Field getField(Class<?> itemClass, String fieldName) {
        var fieldsStream = Stream.of(itemClass.getFields());
        var declaredFieldsStream = Stream.of(itemClass.getDeclaredFields());
        return Stream.concat(fieldsStream, declaredFieldsStream)
                     .filter(check -> check.getName().equals(fieldName)).findFirst()
                     .orElseThrow(() -> new RuntimeException(String.format("Field with name '%s' not found",
                                                                           fieldName)));
    }
}
