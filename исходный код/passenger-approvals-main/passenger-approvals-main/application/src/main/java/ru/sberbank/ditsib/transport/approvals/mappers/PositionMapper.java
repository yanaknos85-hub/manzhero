package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Маппер должностей.
 */
@Mapper
public interface PositionMapper {
    
    Position toModel(PositionMessage message);

    Position positionMessageToPosition(ru.sberbank.ditsib.transport.messaging.messages.PositionMessage source);
}
