package ru.sberbank.ditsib.transport.approvals.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.mappers.DepartmentMapper;
import ru.sberbank.ditsib.transport.approvals.provider.DepartmentProvider;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;
import ru.sberbank.ditsib.transport.approvals.services.DepartmentService;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.approvals.services.TripApproverService;
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
    
    public static final String NO_ORGANIZATION_MESSAGE = "Can't save department id:{}, name:{}, organizationId:{}, organization isn't present";
    public static final String AWAITING_ORGANIZATION_MESSAGE = "Can't save department id:%s, entityName:%s, organizationId:%s, awaiting " +
                                                               "organization synchronization";
    private final DepartmentService service;
    private final DepartmentMapper mapper;
    private final OrganizationService organizationService;
    private final ApproveService<TripRequestApproval> tripApproveService;
    private final TripApproverService approverService;
    
    @Override
    public void delete(DepartmentMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @Override
    public void save(DepartmentMessage message) {
        if (Objects.isNull(message.getOrganizationId())) {
            log.info(NO_ORGANIZATION_MESSAGE,
                     message.getId(),
                     message.getDepartmentName(),
                     message.getOrganizationId());
        } else {
            if (organizationService.get(message.getOrganizationId()).isEmpty()) {
                organizationService.saveGrpcEntity(AWAITING_ORGANIZATION_MESSAGE.formatted(message.getId(),
                                                                                           message.getDepartmentName(),
                                                                                           message.getOrganizationId()),
                                                   message.getOrganizationId());
            }
            var entity = mapper.departmentMessageToDepartment(message);
            service.save(entity);
            tripApproveService.handlingAutoApproving(entity);
            approverService.onDepartmentChanged(entity.getId());
        }
    }
}
