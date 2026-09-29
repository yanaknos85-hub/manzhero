package ru.sberbank.ditsib.corpclient.service.import_easup.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.service.import_easup.AutoApproveService;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Реализация сервиса интеграции.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
class AutoApproveServiceImpl implements AutoApproveService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Override
    public void run() {
        log.debug("IntegrationService: updateAutoApprove: setAutoAssignFlags: start");
        var positions = departmentRepository.findAll().stream()
                .map(Department::getHead)
                .filter(Objects::nonNull)
                .distinct()
                .map(Employee::getPosition)
                .filter(Objects::nonNull)
                .peek(e -> e.setSelfApproved(true))
                .collect(Collectors.toList());
        positionRepository.saveAll(positions);
        log.debug("IntegrationService: updateAutoApprove: setAutoAssignFlags: end");
    }
}
