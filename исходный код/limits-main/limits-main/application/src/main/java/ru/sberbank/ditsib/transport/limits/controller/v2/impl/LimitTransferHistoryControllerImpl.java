package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitTransferHistoryController;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitTransferHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitTransferHistoryMapper;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@RestController("limitTransferHistoryControllerV2Impl")
@Slf4j
@Scope("request")
class LimitTransferHistoryControllerImpl implements LimitTransferHistoryController {

    private final EmployeeService employeeService;

    private final LimitService limitService;

    private final LimitTransferHistoryService limitTransferHistoryService;

    private final LimitTransferHistoryMapper limitTransferHistoryMapper;

    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    @Override
    public GetLimitTransferHistoryV2DTO add(LimitTransferHistoryV2DTO limitTransferHistoryDTO, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getName());
        var author = employeeService.getByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", userId)));

        var source = limitTransferHistoryDTO.sourceLimit();
        var target = limitTransferHistoryDTO.targetLimit();

        var sourceLimit = limitService.get(source).orElseThrow(() -> new EntityNotFoundException(
            Limit.class, source));
        var targetLimit = limitService.get(target).orElseThrow(() -> new EntityNotFoundException(
            Limit.class, target));

        var sourcePeriod = limitTransferHistoryDTO.sourcePeriod();
        var targetPeriod = limitTransferHistoryDTO.targetPeriod();

        var sourceData = new LimitData(sourceLimit, limitTransferHistoryDTO.sourceTransportType(), Period.create(sourcePeriod.name()));
        var targetData = new LimitData(targetLimit, limitTransferHistoryDTO.targetTransportType(), Period.create(targetPeriod.name()));
        final var sum = limitTransferHistoryDTO.sum();
        var limitTransferHistory = limitTransferHistoryService.add(author.getId(),
            sourceData,
            targetData,
                sum,
            limitTransferHistoryDTO.year(),
            limitTransferHistoryDTO.historyType());
        return limitTransferHistoryMapper.toV2Dto(limitTransferHistory.get());
    }

    @Override
    public void delete(UUID limitTransferHistoryId, JwtAuthenticationToken authentication) {
        var limitTransferHistory = limitTransferHistoryService.get(limitTransferHistoryId)
            .orElseThrow(() -> new EntityNotFoundException(LimitTransferHistory.class, limitTransferHistoryId));
        limitTransferHistoryService.delete(limitTransferHistory);
    }

    @Override
    public GetLimitTransferHistoryV2DTO get(UUID limitTransferHistoryId) {
        var limitTransferHistory = limitTransferHistoryService.get(limitTransferHistoryId).orElseThrow(
            () -> new EntityNotFoundException(LimitTransferHistory.class, limitTransferHistoryId));
        return limitTransferHistoryMapper.toV2Dto(limitTransferHistory);
    }

    @Override
    public Page<GetLimitTransferHistoryV2DTO> getAll(int page, int size, Sort.Direction direction, UUID limitId, Integer year) {
        var list = limitTransferHistoryService.getAll(null, limitId, year, page, size, direction);
        return list.map(limitTransferHistoryMapper::toV2Dto);
    }
}
