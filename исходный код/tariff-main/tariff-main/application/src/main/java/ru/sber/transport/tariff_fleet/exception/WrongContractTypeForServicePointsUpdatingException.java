
package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Unsupported contract type")
public class WrongContractTypeForServicePointsUpdatingException extends BusinessException {
    /**
     * Wrong contract type for service-points updating exception
     */
    public WrongContractTypeForServicePointsUpdatingException() {
        super("Нельзя редактировать точки обслуживания по договору с типом интеграции по API");
    }
}
