package ru.sberbank.ditsib.transport.sudir.providers.role.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;

import java.util.Collection;

/**
 * Маппер ролей.
 */
@Mapper(uses = ScopeMapper.class)
public interface RolesMapper {

    /**
     * Convert database to business.
     *
     * @param role database entity.
     * @return business entity.
     */
    Role toBusiness(RoleRecord role);

    /**
     * Convert message to database.
     *
     * @param message message.
     * @return database entity.
     */
    @Mapping(target = "defaultFor", source = "scopes")
    RoleRecord toModel(RoleMessage message);

    /**
     * Convert message to database.
     *
     * @param scopes message.
     * @return database entity.
     */
    @SneakyThrows(JsonProcessingException.class)
    default JSON toModel(@NonNull Collection<String> scopes) {
        return JSON.json(objectMapper().writeValueAsString(scopes));
    }

    /**
     * Get object mapper bean.
     *
     * @return object mapper bean.
     */
    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }
}
