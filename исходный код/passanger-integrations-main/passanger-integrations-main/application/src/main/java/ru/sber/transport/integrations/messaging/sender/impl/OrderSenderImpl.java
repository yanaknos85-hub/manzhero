package ru.sber.transport.integrations.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.integrations.messaging.sender.OrderSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderSenderImpl implements OrderSender {
    
    @Qualifier("carLocationResponseOutput")
    private final ObjectProvider<OutputBridge> carLocationResponseOutput;
    
    @Override
    public void send(OrdersLocationMessage message) {
        Optional.ofNullable(carLocationResponseOutput.getIfAvailable()).ifPresent(ob -> ob.send(message));
    }
}
