package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.transport.messaging.messages.TransportOrgMessage;

/**
 * Маппер транспорта организаций.
 */
@Mapper
public interface TransportOrgMapper {

    @Mapping(target = "deleted", source = "deleted")
    TransportOrgMessage toMessage(TransportOrg transportOrg, boolean deleted);
}
