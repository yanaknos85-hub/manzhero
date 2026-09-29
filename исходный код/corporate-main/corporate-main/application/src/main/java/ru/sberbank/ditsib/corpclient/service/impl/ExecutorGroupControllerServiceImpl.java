package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupParameters;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.mapper.ExecutorGroupMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.ExecutorGroupSender;
import ru.sberbank.ditsib.corpclient.service.ExecutorGroupControllerService;
import ru.sberbank.ditsib.corpclient.service.ExecutorGroupService;

import java.util.List;
import java.util.UUID;

/**
 * Реализация контроллера сервиса групп исполнителей.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutorGroupControllerServiceImpl implements ExecutorGroupControllerService {

    private final ExecutorGroupService service;
    private final ExecutorGroupSender sender;
    private final ExecutorGroupMapper mapper;

    @Override
    public ExecutorGroupDTO add(NewExecutorGroupDTO request) {
        var newExecutorGroup = mapper.toModel(request);
        var executorGroupDTO = service.add(newExecutorGroup);
        sender.send(executorGroupDTO);
        return executorGroupDTO;
    }

    @Override
    public ExecutorGroupDTO get(UUID executorGroupId) {
        return service.get(executorGroupId);
    }

    @Override
    public void update(UUID executorGroupId, NewExecutorGroupDTO newData) {
        var updateExecutorGroup = mapper.toUpdate(newData);
        var executorGroupDTO = service.update(executorGroupId, updateExecutorGroup);
        sender.send(executorGroupDTO);
    }

    @Override
    public void delete(UUID executorGroupId) {
        sender.send(service.delete(executorGroupId));
    }

    @Override
    public Iterable<ExecutorGroupDTO> get(ExecutorGroupParameters parameters) {
        return service.get(parameters);
    }

    @Transactional
    @Override
    public ExecutorGroupDTO getExecutorGroupByEmployeeId(UUID employeeId,  List<UUID> geoZoneIds) {
        return mapper.toDto(service.getExecutorGroupByEmployeeId(employeeId, geoZoneIds, "EMPLOYEE_TRANSPORTATION"));
    }
}
