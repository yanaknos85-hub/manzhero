package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;

public interface RequestApprovalService {

    /**
     * Создание/изменение согласования заявки на маршрут
     */
    void handleRequestMessage(RequestMessage message);


}
