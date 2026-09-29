package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение о причинах поездки.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TripPurposeMessage implements Message<UUID> {
    
    /**
     * ID.
     */
    private UUID id;
    
    /**
     * Label.
     */
    private String label;

    /**
     * Deleted.
     */
    private boolean deleted;

    /**
     * Organization.
     */
    private UUID organization;

}
