package ru.sber.transport.integrations.utils;

import lombok.experimental.UtilityClass;
import org.springframework.messaging.Message;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;

import java.util.Comparator;

@UtilityClass
public class ComparatorUtils {

    public Comparator<Message<OutContractorTaxiTripMessage>> compareOutContractorTaxiTripMessage() {
        return Comparator.comparingInt((Message<OutContractorTaxiTripMessage> m) -> statusToPriority(m.getPayload().status()));
    }

    // Сообщения по созданию и отмене заявки отправляются в первую очередь
    private int statusToPriority(OutboundRequestStatus status) {
        return switch (status) {
            case NEW, REJECT -> 0;
            default -> 1;
        };
    }
}
