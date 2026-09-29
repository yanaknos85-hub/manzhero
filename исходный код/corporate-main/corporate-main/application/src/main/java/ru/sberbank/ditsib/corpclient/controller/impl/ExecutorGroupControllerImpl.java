package ru.sberbank.ditsib.corpclient.controller.impl;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.ExecutorGroupController;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupParameters;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.service.ExecutorGroupControllerService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ExecutorGroupController
 */
@RequiredArgsConstructor
@RestController
public class ExecutorGroupControllerImpl implements ExecutorGroupController {

    public static final String ROLE_ADMIN_EXECUTOR_GROUP = "data_master";

    private final ExecutorGroupControllerService executorGroupService;

    @Override
    public ExecutorGroupDTO saveExecutorGroup(NewExecutorGroupDTO newExecutorGroup) {
        return executorGroupService.add(newExecutorGroup);
    }

    @Override
    public ExecutorGroupDTO getExecutorGroup(UUID executorGroupId, JwtAuthenticationToken authentication) {
        ExecutorGroupDTO executorGroup = executorGroupService.get(executorGroupId);

        executorGroup.setEditable(authentication.getToken().getClaimAsBoolean(ROLE_ADMIN_EXECUTOR_GROUP));

        return executorGroup;
    }

    @Override
    public void deleteExecutorGroup(@PathVariable UUID executorGroupId){
        executorGroupService.delete(executorGroupId);
    }

    @Override
    public void editExecutorGroup(UUID executorGroupId, NewExecutorGroupDTO newData) {
        executorGroupService.update(executorGroupId, newData);
    }

    @Override
    public Iterable<ExecutorGroupDTO> getExecutorGroups(ExecutorGroupParameters parameters) {
        return executorGroupService.get(parameters);
    }

    @Override
    public ExecutorGroupDTO getExecutorGroupByEmployeeId(UUID employeeId, List<UUID> geoZoneIds) {
        return executorGroupService.getExecutorGroupByEmployeeId(employeeId,
                geoZoneIds != null ? geoZoneIds : Collections.emptyList()
        );
    }
}
