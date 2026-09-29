package ru.sberbank.ditsib.corpclient.service.import_easup;

import ru.sberbank.ditsib.corpclient.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для импорта сотрудника из ЕАСУП
 * */
public interface ImportEmployeeService {

    /**
     * Импортировать сотрудника из ЕАСУП или создать пользователя-заглушку
     *
     * @param personnelNumber       табельный номер сотрудника
     * @param userId                идентификатор пользователя в сервисе OAuth
     * @return данные пользователя
     * */
    Optional<Employee> importEmployee(String personnelNumber, UUID userId);
}
