package ru.sber.transport.corporate.web.grpc.client;

import ru.sber.transport.corporate.business.model.Employee;

import java.util.Optional;

/**
 * Клиент синхронизации
 */
public interface SyncClient {

    /**
     * Запрос данных о вошедшем сотруднике в сервисе ЕАСУП.
     *
     * @return данные о сотруднике.
     */
    Optional<Employee> findEntered();

}
