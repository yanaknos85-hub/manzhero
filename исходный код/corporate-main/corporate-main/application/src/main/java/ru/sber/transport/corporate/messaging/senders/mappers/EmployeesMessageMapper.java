package ru.sber.transport.corporate.messaging.senders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sber.transport.corporate.messaging.mappers.AttributeMessageMapper;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Collections;

/**
 * Маппер сообщений сотрудников
 */
@Mapper(uses = {ActiveMessageMapper.class, ContactMessageMapper.class, AttributeMessageMapper.class}, imports = Collections.class)
public interface EmployeesMessageMapper {

    /**
     * Конвертация модели в сообщение.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "mobilePhone", source = "phone")
    @Mapping(target = "marriageCertificateNumber", source = "marriageCertificate")
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "employeeType", source = "type")
    @Mapping(target = "itinerantType", source = "itinerant")
    @Mapping(target = "availableTransportTypes", ignore = true)
    @Mapping(target = "delegateTrait", ignore = true)
    @Mapping(target = "delegatedById", ignore = true)
    EmployeeMessage toMessage(Employee source);

    /**
     * Конвертация сообщения в модель.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "itinerantType", source = "itinerant", defaultValue = "NONE")
    @Mapping(target = "employeeType", source = "type")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "contacts", source = "source")
    @Mapping(target = "attributes", source = "attributes", defaultExpression = "java(Collections.emptyList())")
    ru.sber.transport.messages.corporate.avro.EmployeeMessage toMessageAvro(Employee source);
}
