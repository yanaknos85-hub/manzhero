package ru.sber.transport.authorization.service;

import java.util.UUID;
import java.util.function.Function;

/**
 * Функция получения данных организации сотрудника.
 */
public interface EmployeeOrganizationFunction extends Function<UUID, UUID> {
}
