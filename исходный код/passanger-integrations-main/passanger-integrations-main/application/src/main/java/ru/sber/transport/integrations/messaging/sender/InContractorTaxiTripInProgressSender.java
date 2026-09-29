package ru.sber.transport.integrations.messaging.sender;

import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;

public interface InContractorTaxiTripInProgressSender {

    void send(InContractorTaxiTripInProgressMessage tripDTO);
}
