package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

/**
 * Сообщение с данными транспорта организации.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransportOrgMessage implements Message<UUID> {

    /**
     * Идентификатор транспорта организации.
     */
    private UUID id;

    /**
     * Идентификатор организации.
     */
    private UUID organizationId;

    /**
     * Тип транспорта.
     */
    private TransportTypeEnum transportType;

    /**
     * Deleted.
     */
    private boolean deleted = false;
}