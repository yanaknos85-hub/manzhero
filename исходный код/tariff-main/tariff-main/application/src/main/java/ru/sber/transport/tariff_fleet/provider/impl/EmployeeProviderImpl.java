package ru.sber.transport.tariff_fleet.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sber.transport.tariff_fleet.mapper.EmployeeMapper;
import ru.sber.transport.tariff_fleet.provider.EmployeeProvider;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.EmployeeService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Objects;

/**
 * Реализация провайдера сотрудников.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class EmployeeProviderImpl implements EmployeeProvider {
    
    public static final String ERROR_NOT_IN_DB_MESSAGE_FORMAT = "Can't save employee id:%s, personnelNumber:%s, positionId:%s, " +
                                                      "organizationId:%s, departmentId:%s, awaiting %s synchronization";
    public static final String ERROR_NOT_PRESENT_MESSAGE_FORMAT = "Can't save employee id:%s, personnelNumber:%s, positionId:%s, " +
                                                                          "organizationId:%s, departmentId:%s. %s isn't present.";
    private final EmployeeService service;
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    
    private final EmployeeMapper mapper;
    
    @Override
    public void delete(EmployeeMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @Override
    public void save(EmployeeMessage message) throws AwaitingSynchronizationException {
        if (Objects.isNull(message.getOrganizationId())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPersonnelNumber(),
                                             message.getPositionId(),
                                             message.getOrganizationId(),
                                             message.getDepartmentId(),
                                             "Organization");
            log.info(errorMessage);
        } else if (organizationService.get(message.getOrganizationId()).isEmpty()) {
            var errorMessage = String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPersonnelNumber(),
                                             message.getPositionId(),
                                             message.getOrganizationId(),
                                             message.getDepartmentId(),
                                             "organization");
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else if (Objects.isNull(message.getDepartmentId())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPersonnelNumber(),
                                             message.getPositionId(),
                                             message.getOrganizationId(),
                                             message.getDepartmentId(),
                                             "Department");
            log.info(errorMessage);
        } else if (departmentService.get(message.getDepartmentId()).isEmpty()) {
            var errorMessage = String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPersonnelNumber(),
                                             message.getPositionId(),
                                             message.getOrganizationId(),
                                             message.getDepartmentId(),
                                             "department");
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else if (Objects.isNull(message.getPositionId())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPersonnelNumber(),
                                             message.getPositionId(),
                                             message.getOrganizationId(),
                                             message.getDepartmentId(),
                                             "Position");
            log.info(errorMessage);
        } else if (positionService.get(message.getPositionId()).isEmpty()) {
            var errorMessage = String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPersonnelNumber(),
                                             message.getPositionId(),
                                             message.getOrganizationId(),
                                             message.getDepartmentId(),
                                             "position");
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else {
            service.saveOrUpdate(mapper.employeeMessageToEmployee(message));
        }
    }
}
