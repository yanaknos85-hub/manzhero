package ru.sberbank.ditsib.transport.sudir.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;

/**
 * Маппинг токенов из бизнес в веб.
 */
@Mapper
public interface RoleMapper {

    /**
     * Map business to web.
     *
     * @param source business object.
     * @return web object.
     */
    @Mapping(target = "description", source = "description")
    ru.sberbank.ditsib.transport.sudir.web.model.Role toModel(Role source);
    
}
