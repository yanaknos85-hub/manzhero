package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.corpclient.dto.NewPositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionShortDTO;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Маппер должностей.
 */
@Mapper(uses = { OrganizationMapper.class, DateMapper.class, ActiveStatusMapper.class })
public interface PositionMapper {
    
    PositionShortDTO toDto(Position model);
    
    @Mapping(source = "organization.id", target = "organizationId")
    NewPositionDTO positionToNewDTO(Position position);
    
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "active", source = "activeStatus", qualifiedByName = "active")
    PositionDTO positionToDTO(Position position);
    
    @Mapping(target = "organization", source = "organizationId")
    @Mapping(target = "activeStatus", constant = "ACTIVE")
    Position newDTOToPosition(NewPositionDTO position);
    
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "positionName", source = "name")
    PositionMessage toMessage(PositionDTO position);

    ru.sber.transport.messages.corporate.avro.PositionMessage toMessageAvro(PositionDTO position);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "positionName", source = "name")
    PositionMessage toMessage(Position position);
}
