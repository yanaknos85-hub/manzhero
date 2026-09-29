package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Department in organization not found")
public class DepartmentInOrganizationNotFoundException extends BusinessException {

    public DepartmentInOrganizationNotFoundException(UUID departmentId, UUID organizationId) {
        super(String.format("Подразделение %s не найдено в организации с id:%s", departmentId, organizationId));
    }
}