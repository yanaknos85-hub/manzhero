package ru.sber.transport.integrations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.integrations.dto.ContractorInfoRequest;
import ru.sber.transport.integrations.dto.OrdersLocationResponse;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;

import java.util.UUID;

@Mapper(
        imports = UUID.class
)
public interface CarLocationMapper {

    ContractorInfoRequest carLocationMessageToContractorInfoRequest(CarLocationMessage.ContractorInfo source);
    
    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    OrdersLocationMessage orderLocationResponseToOrdersLocationMessage(OrdersLocationResponse source);
}
