package ru.sber.transport.tariff_fleet.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.provider.ContractorProvider;
import ru.sber.transport.tariff_fleet.service.ContractorService;

import java.util.Objects;

/**
 * Реализация провайдера контрагентов.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class ContractorProviderImpl implements ContractorProvider {
    
    public static final String ERROR_NOT_PRESENT_MESSAGE_FORMAT =
            "Can't save contractor id:%s, contractorName:%s, %s isn't present";
    
    private final ContractorService service;
    
    @Override
    public void delete(ContractorMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @Override
    public void save(ContractorMessage message) {
        if (Objects.isNull(message.getId())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             null,
                                             message.name(),
                                             "contractor's id");
            log.info(errorMessage);
        } else if (Objects.isNull(message.name())) {
            var errorMessage = String.format(ERROR_NOT_PRESENT_MESSAGE_FORMAT,
                                             message.getId(),
                                             null,
                                             "contractor's name");
            log.info(errorMessage);
        } else {
            service.save(new Contractor(message.getId(), message.name(), true,
                                        ContractorType.valueOf(message.contractorType()),
                                        ServiceType.valueOf(message.serviceType())));
        }
    }
}
