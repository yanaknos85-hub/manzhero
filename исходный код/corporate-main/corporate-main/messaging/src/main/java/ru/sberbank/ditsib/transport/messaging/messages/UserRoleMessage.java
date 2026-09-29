package ru.sberbank.ditsib.transport.messaging.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сообщение со связкой пользователь-роли.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleMessage implements Message<UUID> {
    
    /**
     * Пользователь.
     */
    private UUID user;
    
    /**
     * Список ролей.
     */
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return getUser();
    }
}
