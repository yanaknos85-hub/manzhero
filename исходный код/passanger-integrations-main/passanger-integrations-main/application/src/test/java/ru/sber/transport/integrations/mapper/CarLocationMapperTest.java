package ru.sber.transport.integrations.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.integrations.dto.ContractorInfoRequest;
import ru.sber.transport.integrations.dto.OrdersLocationResponse;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarLocationMapperTest {
    
    private final CarLocationMapper mapper = Mappers.getMapper(CarLocationMapper.class);
    
    @Test
    void carLocationMessageToContractorInfoRequest() {
        var source = new CarLocationMessage.ContractorInfo(
                "testUrl",
                "testLogin",
                "testPassword",
                List.of("testId1", "testId2")
        );
        var mappedObject = mapper.carLocationMessageToContractorInfoRequest(source);
        
        assertThat(mappedObject).extracting(
                                        ContractorInfoRequest::url,
                                        ContractorInfoRequest::login,
                                        ContractorInfoRequest::password,
                                        ContractorInfoRequest::orderPartnerIds
                                           )
                                .containsExactly(
                                        source.url(),
                                        source.login(),
                                        source.password(),
                                        source.orderPartnerIds()
                                                );
    }
    
    @Test
    void orderLocationResponseToOrdersLocationMessage() {
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
        var mappedObject = mapper.orderLocationResponseToOrdersLocationMessage(response);
        
        assertThat(mappedObject)
                .isNotNull()
                .extracting(OrdersLocationMessage::id)
                .isNotNull();
        
        assertThat(mappedObject)
                .extracting(OrdersLocationMessage::orderLocations)
                .usingRecursiveComparison()
                .isEqualTo(response.orderLocations());
        
        
    }
}