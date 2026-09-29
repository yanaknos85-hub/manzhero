package ru.sber.transport.corporate.messaging.senders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

/**
 * Маппер данных подразделений.
 */
@Mapper(uses = ActiveMessageMapper.class)
public interface DepartmentMessageMapper {

    /**
     * Конвертация модели в сообщение.
     *
     * @param department исходные данные
     * @return сообщение.
     */
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "departmentHeadId", source = "headId")
    @Mapping(target = "departmentName", source = "name")
    @Mapping(target = "easupId", source = "syncId")
    DepartmentMessage toMessage(Department department);

    /**
     * Конвертация сообщения в модель.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "easupId", source = "syncId")
    ru.sber.transport.messages.corporate.avro.DepartmentMessage toMessageAvro(Department source);
}
