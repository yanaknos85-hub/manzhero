package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Contract not found")
public class ContractNotFoundException extends BusinessException {
    public ContractNotFoundException(UUID id) {
        super(String.format("Не найден договор ID:%s", id));
    }

    public ContractNotFoundException(DocumentType type, UUID id) {
        super("Не найден договор на услуги %s и ТО ID:%s".formatted(type.getLocal(), id));
    }
}
