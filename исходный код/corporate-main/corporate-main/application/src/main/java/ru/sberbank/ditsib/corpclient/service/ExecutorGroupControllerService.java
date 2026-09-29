package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupParameters;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;

import java.util.List;
import java.util.UUID;

/**
 * Сервис контроллера для работы с группами исполнителей.
 */
public interface ExecutorGroupControllerService {

    /**
     * Добавление группы исполнителей.
     *
     * @param executorGroup новая группа исполнителей.
     * @return добавленная группа исполнителей.
     */
    ExecutorGroupDTO add(NewExecutorGroupDTO executorGroup);

    /**
     * Получение группы исполнителей.
     *
     * @param executorGroupId идентификатор группу исполнителей.
     * @return группа исполнителей.
     */
    ExecutorGroupDTO get(UUID executorGroupId);

    /**
     * Обновление группы исполнителей.
     *
     * @param executorGroupId идентификатор группу исполнителей.
     * @param newData новые данные по группе исполнителей.
     */
    void update(UUID executorGroupId, NewExecutorGroupDTO newData);

    /**
     * Получение групп исполнителей.
     *
     * @param parameters пагинация.
     *
     * @return группы исполнителей.
     */
    Iterable<ExecutorGroupDTO> get(ExecutorGroupParameters parameters);

    /**
     * Получение группы исполнителей по id пользователя и id геозон
     *
     * @param employeeId id пользователя
     * @param geoZoneIds id геозон
     *
     * @return DTO с данными группы исполнителей
     */
    ExecutorGroupDTO getExecutorGroupByEmployeeId(UUID employeeId, List<UUID> geoZoneIds);

    /**
     * Удаление группы исполнителей.
     *
     * @param executorGroupId идентификатор группы исполнителей.
     */
    void delete(UUID executorGroupId);
}
