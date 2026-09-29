package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "User has no permission")
public class OrganizationPermissionException extends BusinessException {
    public static final String CONTRACT_PERMISSION_ERROR_MESSAGE = "Пользователь не имеет прав на договор:%s";
    public static final String TARIFF_PERMISSION_ERROR_MESSAGE = "Пользователь не имеет прав на тариф:%s";
    public OrganizationPermissionException(String template, UUID contractId) {
        super(template.formatted(contractId));
    }
}
