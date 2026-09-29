package ru.sber.transport.corporate.messaging.senders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Маппер данных должностей.
 */
@Mapper(uses = ActiveMessageMapper.class)
public interface PositionMessageMapper {

    /**
     * Конвертация модели в сообщение.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "positionName", source = "name")
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "selfApproved", source = "noApproveRequired")
    @Mapping(target = "availableClasses", source = "availableClasses")
    PositionMessage toMessage(Position source);

    /**
     * Конвертация сообщения в модель.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "active", source = "status", qualifiedByName = ActiveMessageMapper.MAP_ACTIVE)
    @Mapping(target = "selfApproved", source = "noApproveRequired")
    ru.sber.transport.messages.corporate.avro.PositionMessage toMessageAvro(Position source);
}
