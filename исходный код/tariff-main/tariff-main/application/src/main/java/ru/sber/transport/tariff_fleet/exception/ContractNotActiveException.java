package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Contract not active")
public class ContractNotActiveException extends BusinessException {

    public static final String EDIT_NOT_ACTIVE_CONTRACT_ERROR_MSG = "Нельзя отредактировать неактивный договор ID:%s";
    public static final String EDIT_TARIFF_OF_NOT_ACTIVE_CONTRACT_ERROR_MSG = "Нельзя отредактировать тариф по неактивному договору ID:%s";
    public static final String CREATE_TARIFF_OF_NOT_ACTIVE_CONTRACT_ERROR_MSG = "Нельзя создать тариф по неактивному договору ID:%s";
    public static final String DEACTIVATE_NOT_ACTIVE_ERROR_MSG = "Договор уже не активен. ИД: %s";
    public static final String DEACTIVATE_CONTRACT_WITH_ACTIVE_TARIFF_ERROR_MSG = "На договоре имеется активный тариф";

    public ContractNotActiveException(String message, UUID id) {
        super(message.formatted(id));
    }

    public ContractNotActiveException(String message) {
        super(message);
    }
}
