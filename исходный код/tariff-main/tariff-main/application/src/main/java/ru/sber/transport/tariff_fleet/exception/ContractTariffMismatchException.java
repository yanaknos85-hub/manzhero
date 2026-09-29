package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Tariff and contract mismatch")
public class ContractTariffMismatchException extends BusinessException {
    public ContractTariffMismatchException(UUID contractId, UUID tariffId) {
        super(String.format("Не найдена связь договора ID:%s и тарифа ID:%s", contractId, tariffId));
    }
}
