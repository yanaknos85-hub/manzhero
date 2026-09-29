package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudListenMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudMessagePlain;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.FraudImpl;
import ru.sber.transport.messages.ai_receipt_scanner.avro.FraudMessage;

@Mapper
public interface FraudMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestId", source = "id")
    FraudImpl toFraud(FraudMessagePlain message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestId", source = "id")
    FraudImpl receiptFraudAvroMessageToFraud(FraudMessage message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestId", source = "id")
    FraudImpl radiusFraudAvroMessageToFraud(ru.sber.transport.messages.fraud.avro.FraudMessage message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestId", source = "request.requestId")
    @Mapping(target = "comment", source = "item.comment")
    @Mapping(target = "fraudType", source = "item.type")
    @Mapping(target = "source", source = "request.source")
    FraudImpl toFraudListenItem(FraudListenMessage request, FraudListenMessage.FraudDataItem item);
}
