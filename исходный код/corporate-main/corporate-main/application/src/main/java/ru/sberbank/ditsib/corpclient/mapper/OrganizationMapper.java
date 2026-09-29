package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.corpclient.dto.NewOrganizationDTO;
import ru.sberbank.ditsib.corpclient.dto.OrganizationDTO;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.OrganizationExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.OrganizationSelectDTO;
import ru.sberbank.ditsib.corpclient.dto.OrganizationShortDTO;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.UUID;

/**
 * Маппер организаций.
 */
@Mapper(uses = { DepartmentMapper.class, PositionMapper.class, ContactMapper.class})
public interface OrganizationMapper {
    
    @Mapping(target = "positions", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "cargoTypes", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "tripPurposes", ignore = true)
    @Mapping(target = "syncId", source = "easupId")
    Organization toModel(NewOrganizationDTO source);

    @Mapping(target = "easupId", source = "syncId")
    OrganizationDTO toDto(Organization source);

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "easupId", ignore = true)
    @Mapping(target = "organizationGroup", ignore = true)
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "organizationCode", ignore = true)
    OrganizationSelectDTO toSelectDto(Organization source);
    
    @Mapping(target = "deleted", source = "status")
    @Mapping(target = "contacts", source = "contacts")
    OrganizationMessage toMessage(Organization organization);

    @Mapping(target = "deleted", source = "status")
    @Mapping(target = "contacts", source = "contacts")
    @Mapping(target = "name", source = "officialName")
    ru.sber.transport.messages.corporate.avro.OrganizationMessage toMessageAvro(Organization organization);

    default boolean mapToDeleted(ru.sberbank.ditsib.corpclient.database.model.OrganizationStatus status) {
        return ru.sberbank.ditsib.corpclient.database.model.OrganizationStatus.INACTIVE.equals(status);
    }

    OrganizationShortDTO organizationToShortDTO(Organization source);
    
    Organization shortDTOToOrganization(OrganizationShortDTO source);
    
    NewOrganizationDTO organizationToNewDTO(Organization source);

    OrganizationExecutorGroupDTO organizationToExecutorGroupDTO(Organization organization);
    
    @Mapping(target = "id", source = "uuid")
    Organization organizationFromId(UUID uuid);
}
