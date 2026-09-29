package ru.sber.transport.tariff_fleet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Contract already exists in database")
public class ContractAlreadyExistsException extends BusinessException{

    public static final String MSG_FORMAT_1 = "Ошибка в полях Контрагент, Номер договора. Данное сочетание значений contractorId='%s' и " +
            "number='%s' уже имеется в системе";
    public static final String MSG_FORMAT_2 = "Ошибка в поле Номер договора УВХД. Данное значение uvhd='%s' уже имеется в системе";
    public static final String MSG_FORMAT_3 = "В системе уже есть договор с такими же Организацией Контрагентом и Номером. ИД Договора:%s";
    
    public ContractAlreadyExistsException(UUID contractorId, String contractNumber) {
        super(String.format(MSG_FORMAT_1, contractorId, contractNumber));
    }
    public ContractAlreadyExistsException(UUID contractId) {
        super(MSG_FORMAT_3.formatted(contractId));
    }
    
    public ContractAlreadyExistsException(String uvhd) {
        super(String.format(MSG_FORMAT_2, uvhd));
    }
}