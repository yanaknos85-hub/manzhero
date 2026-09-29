package ru.sber.transport.tariff_fleet.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sber.transport.tariff_fleet.mapper.PositionMapper;
import ru.sber.transport.tariff_fleet.provider.PositionProvider;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Objects;

/**
 * Реализация провайдера должностей.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class PositionProviderImpl implements PositionProvider {
    
    public static final String ERROR_NOT_IN_DB_MESSAGE_FORMAT = "Can't save position id:%s, positionName:%s, organizationId:%s awaiting %s " +
            "synchronization";
    public static final String ERROR_NOT_PRESENT_MESSAGE_FORMAT = "Can't save position id:%s, positionName:%s, organizationId:%s, " +
                                                                  "%s id isn't present";
    private final PositionService service;
    private final PositionMapper mapper;
    private final OrganizationService organizationService;
    
    @Override
    public void delete(PositionMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @Override
    public void save(PositionMessage message) throws AwaitingSynchronizationException {
        if (Objects.isNull(message.getOrganizationId())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPositionName(),
                                             message.getOrganizationId(),
                                             "Organization");
            log.info(errorMessage);
        } else if (organizationService.get(message.getOrganizationId()).isEmpty()) {
            var errorMessage = String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                             message.getId(),
                                             message.getPositionName(),
                                             message.getOrganizationId(),
                                             "organization");
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else {
            service.saveOrUpdate(mapper.positionMessageToPosition(message));
        }
    }
}
