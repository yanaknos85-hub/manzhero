package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Active;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Employee;

import java.util.Collection;

@Mapper
public interface UserMapper {

    @Mapping(target = "id", source = "employee.userId")
    @Mapping(target = "orgStructureType", source = "employee.orgStructureType")
    @Mapping(target = "active", expression = "java(mapActiveStatusToBoolean(employee))")
    @Mapping(target = "phone", source = "employee.mobilePhone")
    ru.sberbank.ditsib.transport.messaging.messages.UserMessage toMessage(Employee employee, Collection<String> roles);

    @Mapping(target = "id", source = "employee.id")
    @Mapping(target = "orgStructureType", source = "employee.type")
    @Mapping(target = "active", expression = "java(mapActiveStatusToBoolean(employee))")
    ru.sberbank.ditsib.transport.messaging.messages.UserMessage toMessage(ru.sber.transport.corporate.business.model.Employee employee, Collection<String> roles);

    default boolean mapActiveStatusToBoolean(Employee employee){
        return ActiveStatus.ACTIVE.equals(employee.getActiveStatus());
    }

    default boolean mapActiveStatusToBoolean(ru.sber.transport.corporate.business.model.Employee employee){
        return Active.ACTIVE.equals(employee.getStatus());
    }

}
