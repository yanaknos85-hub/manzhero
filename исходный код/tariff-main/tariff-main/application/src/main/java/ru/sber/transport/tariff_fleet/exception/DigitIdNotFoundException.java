package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Не найден числовой идентификатор организации по идентификатору подразделения
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Digit id not found")
public class DigitIdNotFoundException extends BusinessException {
    
    public DigitIdNotFoundException(UUID departmentId) {
        super(String.format("Не найден уникальный числовой идентификатор организации сотрудника, userId:%s", departmentId));
    }
}
