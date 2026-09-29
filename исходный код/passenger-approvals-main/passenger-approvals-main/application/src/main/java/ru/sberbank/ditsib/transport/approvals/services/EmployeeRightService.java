package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.BaseRequestApproval;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис, содержащий методы, касающиеся прав пользователя на согласования
 */
public interface EmployeeRightService {
    Map<String, Collection<UUID>> getAllowedDepartments(UUID userId);

    Employee checkEmployeeRights(UUID userId, BaseRequestApproval approval);
}
