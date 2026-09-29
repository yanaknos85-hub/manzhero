package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.tariff_fleet.database.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Маппер должностей.
 */
@Mapper(componentModel = "spring")
public interface PositionMapper {

    Position positionMessageToPosition(PositionMessage source);
}
