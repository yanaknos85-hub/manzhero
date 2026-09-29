package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Message of frequently address.
 */
@Jacksonized
@Builder
@Getter
public class FrequentlyAddressMessage implements Message<UUID> {
    
    /**
     * ID.
     */
    private final UUID id;
    
    /**
     * Count.
     */
    private final int count;
    
    /**
     * Country.
     */
    private final String country;
    
    /**
     * Region.
     */
    private final String region;
    
    /**
     * City.
     */
    private final String city;
    
    /**
     * Street.
     */
    private final String street;
    
    /**
     * House.
     */
    private final String house;
    
    /**
     * Building.
     */
    private final String building;
    
    /**
     * Deleted.
     */
    private final boolean deleted;
    
    /**
     * Structure.
     */
    private final String structure;
    
    /**
     * Latitude.
     */
    private final double latitude;
    
    /**
     * Longitude.
     */
    private final double longitude;
    
    /**
     * ID of employee.
     */
    private final UUID employeeId;
    
}
