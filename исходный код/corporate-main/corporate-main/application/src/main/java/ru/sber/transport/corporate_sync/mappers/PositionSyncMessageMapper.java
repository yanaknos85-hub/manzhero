package ru.sber.transport.corporate_sync.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sber.transport.messages.easup.avro.PositionData;

/**
 * Маппер сообщений должностей
 */
@Mapper(uses = ActiveMessageMapper.class)
public interface PositionSyncMessageMapper extends MessageMapper<Position, PositionData> {

    /**
     * Конвертация сообщения в модель.
     *
     * @param source сообщение
     * @param target объект для обновления
     */
    @Override
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "noApproveRequired", ignore = true)
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "syncId", source = "id")
    void update(@MappingTarget Position target, PositionData source);

    /**
     * Конвертация сообщения в модель.
     *
     * @param source сообщение
     * @return модель
     */
    @Override
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "noApproveRequired", source = "chief")
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "syncId", source = "id")
    Position toBusiness(PositionData source);

}
