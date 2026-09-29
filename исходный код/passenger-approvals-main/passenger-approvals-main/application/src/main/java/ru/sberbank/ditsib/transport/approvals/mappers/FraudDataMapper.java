package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.approvals.database.model.FraudData;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;

import java.util.List;

@Mapper
public interface FraudDataMapper {

    FraudData toEntity(RequestMessage.Fraud fraud);

    List<FraudData> toEntities(List<RequestMessage.Fraud> frauds);
}
