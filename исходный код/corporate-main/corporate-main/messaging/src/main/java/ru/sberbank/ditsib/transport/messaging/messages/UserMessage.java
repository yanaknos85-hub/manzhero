package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.Builder;
import ru.sber.transport.messaging.Message;

import java.util.Set;
import java.util.UUID;

/**
 * Сообщение с данными пользователя.
 *
 * @param id идентификатор.
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param personnelNumber ТН.
 * @param login логин.
 * @param hash хэш пароля.
 * @param consent согласие на обработку.
 * @param deleted признак удаления.
 * @param active признак активности.
 * @param roles роли.
 * @param transportAccess транспортный доступ.
 * @param email почта.
 * @param orgStructureType тип орг. структуры.
 * @param scope область видимости пользователя.
 */
@Builder
public record UserMessage(

        UUID id,

        String lastName,

        String firstName,

        String patronymic,

        String personnelNumber,

        String login,

        String hash,

        boolean consent,

        boolean deleted,

        boolean active,

        Set<String> roles,

        boolean transportAccess,

        String email,

        String orgStructureType,

        Scope scope,

        String phone

) implements Message<UUID> {

    /**
     * Тип пользователя.
     */
    public static final String TYPE = "type";

    @Override
    public UUID getId() {
        return id();
    }

    public enum Scope {
        EMPLOYEE,
        DISPATCHER,
        DRIVER,
        CONTRACTOR
    }
}
