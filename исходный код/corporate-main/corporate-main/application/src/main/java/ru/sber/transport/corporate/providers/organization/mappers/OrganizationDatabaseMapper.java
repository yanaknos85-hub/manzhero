package ru.sber.transport.corporate.providers.organization.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;

/**
 * Маппер объектов базы данных в бизнес.
 */
@Mapper
public interface OrganizationDatabaseMapper {

    /**
     * Маппер бизнес объекта в базу данных.
     *
     * @param source бизнес объект.
     * @return бизнес объект в базе данных.
     */
    @Mapping(target = "officialName", source = "name")
    @Mapping(target = "organizationCode", source = "code")
    @Mapping(target = "tin", source = "tid")
    @Mapping(target = "organizationGroupId", ignore = true)
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "table", ignore = true)
    OrganizationRecord toDatabase(Organization source);

    /**
     * Маппер объекта базы данных в бизнес.
     *
     * @param source бизнес объект в базе данных.
     * @return бизнес объект.
     */
    @Mapping(target = "tid", source = "tin")
    @Mapping(target = "name", source = "officialName")
    @Mapping(target = "code", source = "organizationCode")
    @Mapping(target = "groupId", source = "organizationGroupId")
    @Mapping(target = "availableClasses", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    Organization toBusiness(OrganizationRecord source);
}
