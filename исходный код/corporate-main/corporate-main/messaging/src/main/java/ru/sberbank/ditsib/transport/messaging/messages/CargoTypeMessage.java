package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение с типом груза.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoTypeMessage implements Message<UUID> {
    
    /**
     * ID..
     */
    private UUID id;
    
    /**
     * name.
     */
    private String name;

    /**
     * Deleted.
     */
    private boolean deleted;
}
