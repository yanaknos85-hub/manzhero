package ru.sber.transport.integrations.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.integrations.dto.ContractorInfoRequest;
import ru.sber.transport.integrations.dto.CredentialClient;
import ru.sber.transport.integrations.dto.OrdersLocationResponse;
import ru.sber.transport.integrations.mapper.CarLocationMapper;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.integrations.messaging.sender.OrderSender;
import ru.sber.transport.integrations.service.OrderService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CarLocationServiceImplTest {
    
    @Mock
    private OrderService orderService;
    @Mock
    private OrderSender orderSender;
    @Mock
    private CarLocationMapper carLocationMapper;
    @InjectMocks
    private CarLocationServiceImpl carLocationService;
    
    @Captor
    private ArgumentCaptor<OrdersLocationMessage> ordersLocationMessageArgumentCaptor;
    
    @Test
    void getOrdersLocation() {
        var request = new ContractorInfoRequest(
                "http://test-contracotr-api.ru",
                "login",
                "password",
                List.of("firstOrderPartnerId", "secondOrderPartnerId")
        );
        var credentialClient = new CredentialClient(URI.create(request.url()), request.login(), request.password());
        var response = new OrdersLocationResponse(
                List.of(
                        new OrdersLocationResponse.OrderLocation(
                                "firstOrderPartnerId",
                                new OrdersLocationResponse.OrderCoordinates(
                                        10.0003,
                                        10.0004
                                ),
                                3600
                        ),
                        new OrdersLocationResponse.OrderLocation(
                                "secondOrderPartnerId",
                                new OrdersLocationResponse.OrderCoordinates(
                                        10.0005,
                                        10.0006
                                ),
                                1200
                        )
                       )
        );
        var message = new OrdersLocationMessage(
                UUID.randomUUID(),
                List.of(
                        new OrdersLocationMessage.OrderLocationMessage(
                                "firstOrderPartnerId",
                                new OrdersLocationMessage.OrderCoordinatesMessage(
                                        10.0003,
                                        10.0004
                                ),
                                3600
                        ),
                        new OrdersLocationMessage.OrderLocationMessage(
                                "secondOrderPartnerId",
                                new OrdersLocationMessage.OrderCoordinatesMessage(
                                        10.0003,
                                        10.0004
                                ),
                                1200
                        )
                       )
        );
        
        doReturn(response).when(orderService).getOrdersLocation(credentialClient, request.orderPartnerIds());
        doReturn(message).when(carLocationMapper).orderLocationResponseToOrdersLocationMessage(response);
        doNothing().when(orderSender).send(ordersLocationMessageArgumentCaptor.capture());
        
        carLocationService.getOrdersLocation(request);
        
        var sentResponse = ordersLocationMessageArgumentCaptor.getValue();
        assertThat(sentResponse.orderLocations()).hasSize(2)
                                                 .usingRecursiveComparison()
                                                 .isEqualTo(message.orderLocations());
    }
}
