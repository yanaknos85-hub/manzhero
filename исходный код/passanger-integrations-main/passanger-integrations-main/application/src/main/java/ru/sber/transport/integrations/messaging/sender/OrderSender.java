package ru.sber.transport.integrations.messaging.sender;

import ru.sber.transport.integrations.messaging.OrdersLocationMessage;

public interface OrderSender {
    
    void send(OrdersLocationMessage message);
}
