package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Organization medical license is empty")
public class OrganizationMedicalLicenseEmptyException extends BusinessException {
    
    public OrganizationMedicalLicenseEmptyException() {
        super("Нет данных о медицинской лицензии");
    }
}
