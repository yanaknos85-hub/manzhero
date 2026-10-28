package ru.sberbank.ditsib.transport.sudir.messaging.sender.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.sudir.messages.AccountMessage;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mapper of scim messages.
 */
@Mapper
public interface AccountScimMessageMapper {

    /**
     * Convert business to message.
     *
     * @param account business data.
     * @param loggedIn date of logged in.
     * @return message.
     */
    @Mapping(target = "lastLoginDate", source = "loggedIn")
    @Mapping(target = "roles", source = "account.roles")
    AccountMessage toMessage(Account account, LocalDateTime loggedIn);

    /**
     * Convert business to message.
     *
     * @param source business data.
     * @return message.
     */
    default List<String> toMessageRoles(Map<String, Boolean> source) {
        if (source == null) {
            return List.of();
        }
        return new LinkedList<>(source.keySet());
    }

    /**
     * Convert business to delete message.
     *
     * @param id ID of deleted user.
     * @return message.
     */
    AccountMessage toDeleted(UUID id);

    /**
     * Convert business to message.
     *
     * @param source business data.
     * @return message.
     */
    default String toMessage(UUID source) {
        return Optional.ofNullable(source).map(UUID::toString).orElse(null);
    }
}
