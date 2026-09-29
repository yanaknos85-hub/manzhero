package ru.sber.transport.corporate_sync.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sber.transport.corporate.messaging.mappers.DateMessageMapper;
import ru.sber.transport.messages.easup.avro.DepartmentData;

/**
 * Маппер сообщений подразделений
 */
@Mapper(uses = {DateMessageMapper.class, ActiveMessageMapper.class})
public interface DepartmentSyncMessageMapper extends MessageMapper<Department, DepartmentData> {

    /**
     * Конвертация сообщения в модель
     *
     * @param source исходные данные
     * @param target цель для обновления
     */
    @Override
    @Mapping(target = "handmade", constant = "false")
    @Mapping(target = "headId", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "syncId", source = "id")
    void update(@MappingTarget Department target, DepartmentData source);

    /**
     * Конвертация сообщения в модель
     *
     * @param source исходные данные
     * @return бизнес-данные
     */
    @Override
    @Mapping(target = "handmade", constant = "false")
    @Mapping(target = "headId", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "status", source = "active")
    @Mapping(target = "structureType", constant = "INTERNAL")
    @Mapping(target = "syncId", source = "id")
    Department toBusiness(DepartmentData source);
}
