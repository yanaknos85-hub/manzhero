package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;
import ru.sberbank.ditsib.transport.messaging.messages.ExecutorGroupMessage;

import java.util.UUID;

/**
 * Маппер групп исполнителей.
 */
@Mapper(uses = { DepartmentMapper.class, OrganizationMapper.class, EmployeeMapper.class, GeoZoneMapper.class})
public interface ExecutorGroupMapper {

    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "active", ignore = true)
    ExecutorGroup toModel(NewExecutorGroupDTO source);


    ExecutorGroup toUpdate(NewExecutorGroupDTO source);

    @Mapping(target = "updatedAt", source = "updateTime")
    ExecutorGroupDTO toDto(ExecutorGroup source);

    ExecutorGroupMessage toMessage(ExecutorGroupDTO source);

}
