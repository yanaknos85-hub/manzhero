package ru.sber.transport.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.dto.ContractorInfoRequest;
import ru.sber.transport.integrations.dto.CredentialClient;
import ru.sber.transport.integrations.mapper.CarLocationMapper;
import ru.sber.transport.integrations.messaging.sender.OrderSender;
import ru.sber.transport.integrations.service.CarLocationService;
import ru.sber.transport.integrations.service.OrderService;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarLocationServiceImpl implements CarLocationService {
    
    private final OrderService orderService;
    private final OrderSender orderSender;
    private final CarLocationMapper carLocationMapper;
    
    @Override
    public void getOrdersLocation(ContractorInfoRequest contractorInfoRequest) {
        var credentialClient = new CredentialClient(URI.create(contractorInfoRequest.url()),
                                                    contractorInfoRequest.login(),
                                                    contractorInfoRequest.password()
                                                    );
        var response = orderService.getOrdersLocation(
                credentialClient,
                contractorInfoRequest.orderPartnerIds()
                                                     );
        orderSender.send(carLocationMapper.orderLocationResponseToOrdersLocationMessage(response));
    }
}
