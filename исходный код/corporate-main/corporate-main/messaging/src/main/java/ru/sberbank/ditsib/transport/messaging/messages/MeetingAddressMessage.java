package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение о филиалах.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingAddressMessage implements Message<UUID> {
    
    /**
     * Идентификатор.
     */
    private UUID id;
    
    /**
     * Описание.
     */
    private String label;
    
    /**
     * Country.
     */
    private String country;
    
    /**
     * Region.
     */
    private String region;
    
    /**
     * City.
     */
    private String city;
    
    /**
     * Street.
     */
    private String street;
    
    /**
     * House.
     */
    private String house;
    
    /**
     * Building.
     */
    private String building;
    
    /**
     * Structure.
     */
    private String structure;
    
    
}
