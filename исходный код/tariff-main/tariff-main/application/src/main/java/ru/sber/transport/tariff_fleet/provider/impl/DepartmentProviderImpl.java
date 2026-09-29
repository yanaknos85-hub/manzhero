package ru.sber.transport.tariff_fleet.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sber.transport.tariff_fleet.mapper.DepartmentMapper;
import ru.sber.transport.tariff_fleet.provider.DepartmentProvider;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.Objects;

/**
 * Реализация провайдера подразделений.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class DepartmentProviderImpl implements DepartmentProvider {
    
    public static final String ERROR_NOT_IN_DB_MESSAGE_FORMAT =
            "Can't save department id:%s, departmentName:%s, organizationId:%s awaiting %s synchronization";
    public static final String ERROR_NOT_PRESENT_MESSAGE_FORMAT =
            "Can't save department id:%s, departmentName:%s, organizationId:%s, %s isn't present";
    private final DepartmentService service;
    private final DepartmentMapper mapper;
    private final OrganizationService organizationService;
    
    @Override
    public void delete(DepartmentMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @Override
    public void save(DepartmentMessage message) {
        if (Objects.isNull(message.getOrganizationId())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getDepartmentName(),
                                             message.getOrganizationId(),
                                             "Organization");
            log.info(errorMessage);
        } else if (organizationService.get(message.getOrganizationId()).isEmpty()) {
            var errorMessage = String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getDepartmentName(),
                                             message.getOrganizationId(),
                                             "organization");
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else if (Objects.nonNull(message.getParentId()) && service.get(message.getParentId()).isEmpty()) {
            var errorMessage = String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getDepartmentName(),
                                             message.getParentId(),
                                             "parent");
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else {
            service.saveOrUpdate(mapper.departmentMessageToDepartment(message));
        }
    }
}
