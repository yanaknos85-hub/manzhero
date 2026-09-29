package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение со временами доставки.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoDeliveryTimeMessage implements Message<UUID> {
    
    /**
     * ID..
     */
    private UUID id;
    
    /**
     * Label.
     */
    private String label;
    
    /**
     * Urgency.
     */
    private String urgency;
    
    /**
     * Start of interval.
     */
    private Integer start;
    
    /**
     * End of interval.
     */
    private Integer end;
    
    /**
     * Default value.
     */
    private Integer defaultValue;
    
    /**
     * Value.
     */
    private Integer value;

    /**
     * Deleted.
     */
    private boolean deleted;
}
