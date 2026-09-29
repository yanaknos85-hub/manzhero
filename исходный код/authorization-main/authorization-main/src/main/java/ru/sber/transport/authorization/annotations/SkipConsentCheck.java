package ru.sber.transport.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пропускания проверки на факт подписания ПДн.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SkipConsentCheck {

    /**
     * Подстрока урла, при нахождении которой в урле запроса будет пропущена проверка.
     * Работает в случае, если аннотация указана на классе.
     *
     * @return подстрока укрла.
     */
    String value() default "";
}
