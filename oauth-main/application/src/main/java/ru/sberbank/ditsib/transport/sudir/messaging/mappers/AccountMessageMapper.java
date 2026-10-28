package ru.sberbank.ditsib.transport.sudir.messaging.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.lang.NonNull;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper of accounts messages.
 */
@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface AccountMessageMapper {

    /**
     * Convert message to business.
     *
     * @param message message.
     * @return business.
     */
    default Account toBusiness(UserMessage message) {
        Map<String, Boolean> roles = null;
        if (message.roles() != null) {
            roles = message.roles().stream().collect(Collectors.toMap(Function.identity(), string -> false));
        }
        return Account.builder()
                .id(message.id())
                .personnelNumber(message.personnelNumber())
                .roles(roles)
                .active(message.active())
                .scope(Optional.ofNullable(message.scope()).map(Enum::name).orElse("EMPLOYEE"))
                .build();
    }

    /**
     * Convert message to business.
     *
     * @param role role code.
     * @return business.
     */
    @Mapping(target = "code", source = "role")
    Role toBusiness(String role);

    /**
     * Convert message to business.
     *
     * @param roles set of role codes.
     * @return set of business entities.
     */
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<Role> toBusiness(Set<String> roles);

}
