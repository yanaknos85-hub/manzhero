package ru.sber.transport.corporate.messaging.senders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapper;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Маппер сообщений организаций
 */
@Mapper(uses = ActiveMessageMapper.class)
public interface OrganizationMessageMapper {

    /**
     * Конвертация модели в сообщение.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "officialName", source = "name")
    @Mapping(target = "organizationCode", source = "code")
    @Mapping(target = "organizationGroup", ignore = true)
    OrganizationMessage toMessage(Organization source);

    /**
     * Конвертация сообщения в модель.
     *
     * @param source исходные данные
     * @return сообщение.
     */
    @Mapping(target = "deleted", source = "status", qualifiedByName = ActiveMessageMapper.MAP_DELETED)
    @Mapping(target = "group", source = "groupId")
    ru.sber.transport.messages.corporate.avro.OrganizationMessage toMessageAvro(Organization source);
}
