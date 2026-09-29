package ru.sber.transport.limits.web.grpc.client;

import ru.sber.transport.limits.business.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Клиент для работы с оргштатной структурой
 */
public interface StateClient {

    /**
     * Получить данные сотрудника по идентификатору
     *
     * @param id идентификатор сотрудника
     * @return данные сотрудника
     */
    Optional<Employee> get(UUID id);

}
