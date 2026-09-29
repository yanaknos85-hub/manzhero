package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "department is not assigned to contract organization")
public class DepartmentIsNotAssignedToContractOrganizationException extends BusinessException {

    public DepartmentIsNotAssignedToContractOrganizationException() {
        super("Подразделение должно относиться к той же организации, что и договор");
    }
}