package ru.sber.transport.authorization.annotations;

import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для методов, которые подразумевают проверку полномочий пользователя на работу с разными организациями.
 * Работает в комбинации с {@link Organization} и {@link EmployeeOrganizationFunction}. Пользователь может производить действие если:
 * <ol>
 *      <li>
 *          1. Он находится в организации, данные по которой запрашивает/изменяет, либо
 *      </li>
 *      <li>
 *          2. Он является пользователем системы мастер данных.
 *      </li>
 * </ol>
 *
 * @see Organization указывает на поле, где располагаются данные для проверки.
 * @see EmployeeOrganizationFunction описывает логику получения организации пользователя.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckOrganizationAccess {
}
