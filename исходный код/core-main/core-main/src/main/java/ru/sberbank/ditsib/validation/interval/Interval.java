package ru.sberbank.ditsib.validation.interval;

import ru.sberbank.ditsib.validation.interval.validator.IntervalValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Проверка интервала.
 */
@Constraint(validatedBy = IntervalValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interval {
    
    /**
     * @return название поля начала интервала.
     */
    String startField();

    /**
     * @return стратегия проверки начала интервала. По-умолчанию DateNullStrategy.STRICT.
     */
    DateNullStrategy startNullStrategy() default DateNullStrategy.STRICT;

    /**
     * @return стратегия проверки окончания интервала. По-умолчанию DateNullStrategy.STRICT.
     */
    DateNullStrategy endNullStrategy() default DateNullStrategy.STRICT;
    
    /**
     * @return название поля окончания интервала.
     */
    String endField();
    
    /**
     * @return сообщение об ошибке интервала.
     */
    String message() default "Value of start field cannot be greater than value of end field";
    
    /**
     * @return признак включения пограничных значений в сравнение.
     */
    Include inclusion() default Include.NONE;
    
    /**
     * @return группы валидации.
     */
    Class<?>[] groups() default {};
    
    /**
     * @return полезная нагрузка валидации.
     */
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Defines several {@link NotNull} annotations on the same element.
     *
     * @see jakarta.validation.constraints.NotNull
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {

        /**
         * @return массив аннотаций.
         */
        NotNull[] value();
    }
    
    /**
     * Включение пограничных значений.
     */
    enum Include {
    
        /**
         * Не включать.
         */
        NONE,
    
        /**
         * Включать.
         */
        INCLUDE
    }

    /**
     * Стратегия проверки даты.
     */
    enum DateNullStrategy {

        /**
         * Строгая проверка от-до. Если пограничные значения отсутствуют, считается, что интервал не валиден.
         */
        STRICT,

        /**
         * Проверка от-до. Если пограничные значения отсутствуют, проверяется текущая дата.
         */
        NOW
    }
}
