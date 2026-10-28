package ru.sberbank.ditsib.transport.sudir.providers.role.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.sudir.providers.role.model.Scope;

import java.util.Optional;

/**
 * Mapper of scopes.
 */
@Mapper
public interface ScopeMapper {

    /**
     * Convert web to model.
     *
     * @param scope source.
     * @return model
     */
    default Scope toModel(String scope) {
        return Optional.ofNullable(scope).map(Scope::valueOf).orElse(null);
    }

    /**
     * Convert model to web.
     *
     * @param scope model.
     * @return web
     */
    default String toDto(Scope scope) {
        return Optional.ofNullable(scope).map(Scope::name).orElse(null);
    }

}
