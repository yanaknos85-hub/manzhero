package ru.sberbank.ditsib.corpclient.service;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupParameters;

import java.util.List;
import java.util.UUID;

/**
 * Executor group crud and other operations
 */
public interface ExecutorGroupService {

    /**
     * Добавить группу исполнителей.
     *
     * @param model данные новой группы исполнителей.
     * @return добавленая группа исполнителей.
     */
    ExecutorGroupDTO add(ExecutorGroup model);

    /**
     * @param id identifier of executor group
     *
     * @return found executor group
     */
    ExecutorGroupDTO get(@NotNull UUID id);

    /**
     * @param executorGroupId identifier of executor group
     * @param newData data for update executor group
     *
     */
    ExecutorGroupDTO update(@NotNull UUID executorGroupId, @NotNull ExecutorGroup newData);

    /**
     * @param executorGroupId identifier of executor group
     *
     */
    ExecutorGroupDTO delete(@NotNull UUID executorGroupId);

    /**
     * @param parameters pagination
     *
     * @return found executor groups
     */
    Iterable<ExecutorGroupDTO> get(ExecutorGroupParameters parameters);

    /**
     * Получение группы исполнителей по id пользователя и id геозон
     *
     * @param employeeId id пользователя
     * @param geoZoneIds id геозон
     * @param service тип сервиса
     * @return DTO с данными группы исполнителей
     */
    ExecutorGroup getExecutorGroupByEmployeeId(UUID employeeId, List<UUID> geoZoneIds, String service);
}
