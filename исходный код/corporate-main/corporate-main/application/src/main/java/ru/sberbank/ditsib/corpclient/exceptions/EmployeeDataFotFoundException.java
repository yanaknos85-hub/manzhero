package ru.sberbank.ditsib.corpclient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение в случае, если не удалось найти данные для сотрудника
 * */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class EmployeeDataFotFoundException extends RuntimeException{

    /**
     * @param personNumber  табельный номер сотрудника
     * */
    public EmployeeDataFotFoundException(String personNumber) {
        super("Данные для сотрудника ТН %s не найдены".formatted(personNumber));
    }
}
