package ru.sber.transport.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для параметров метода, подразумевающего проверку полномочий пользователя на работы с разными
 * организациями. Указывает на параметр организации. Параметр, отмеченный этой аннотацией, будет использоваться для
 * определения принадлежности организации.
 *
 * @see CheckOrganizationAccess
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Organization {
    
    /**
     * Имя поля, в котором находятся данные организации. Поддерживается только один уровень вложенности (поле должно
     * находиться непосредственно в аннотированном параметре). Если не указано или пустое, берется значение самого
     * аргумента.
     *
     * @return имя поля.
     */
    String value() default "";
    
}
