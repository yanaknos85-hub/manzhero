package ru.sber.transport.corporate.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.web.model.NewOrganizationData;
import ru.sber.transport.web.model.OrganizationData;

/**
 * Веб-маппер организаций
 */
@Mapper
public interface OrganizationWebMapper {

    /**
     * Преобразовать веб в бизнес
     *
     * @param source исходные данные
     * @return бизнес организация
     */
    @Mapping(target = "name", source = "officialName")
    @Mapping(target = "code", source = "organizationCode")
    @Mapping(target = "syncId", source = "easupId")
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groupId", ignore = true)
    Organization toBusiness(NewOrganizationData source);

    /**
     * Преобразовать бизнес в веб
     *
     * @param source исходные данные
     * @return веб организация
     */
    @Mapping(target = "officialName", source = "name")
    @Mapping(target = "organizationCode", source = "code")
    @Mapping(target = "easupId", source = "syncId")
    @Mapping(target = "organizationGroup", ignore = true)
    OrganizationData toWeb(Organization source);

}
