package ru.sber.transport.integrations.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.integrations.mapper.CarLocationMapper;
import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;
import ru.sber.transport.integrations.provider.CarLocationProvider;
import ru.sber.transport.integrations.service.CarLocationService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarLocationProviderImpl implements CarLocationProvider {
    
    private final CarLocationMapper carLocationMapper;
    private final CarLocationService carLocationService;
    
    @Override
    public void getCarLocation(Map<UUID, CarLocationMessage.ContractorInfo> contractorRequests) {
        for (var contractorRequest : contractorRequests.values()) {
            var contractorInfoRequest = carLocationMapper.carLocationMessageToContractorInfoRequest(contractorRequest);
            try {
                carLocationService.getOrdersLocation(contractorInfoRequest);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error("Error while getting car location for contractor: {} with orders: {}", contractorRequest.url(), contractorRequest.orderPartnerIds());
            }
        }
    }
}
