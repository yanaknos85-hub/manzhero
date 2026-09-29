package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.tariff_fleet.constant.ServiceType;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Contractor service type mismatch")
public class ContractorServiceTypeMismatchException extends BusinessException {
    
    public ContractorServiceTypeMismatchException(UUID id, ServiceType actualType, ServiceType expectedType) {
        super(String.format("Вид услуги Контрагента %s  - %s, ожидается %s", id, actualType, expectedType));
    }
}
