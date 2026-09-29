package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Маппер организаций.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    Organization organizationMessageToOrganization(OrganizationMessage source);
}
