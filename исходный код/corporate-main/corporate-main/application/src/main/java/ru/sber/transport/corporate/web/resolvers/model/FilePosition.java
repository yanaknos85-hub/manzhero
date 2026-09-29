package ru.sber.transport.corporate.web.resolvers.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data transfer object with data about position.
 */
@Getter
@Setter
@ToString
public class FilePosition {
    
    /**
     * Organization
     */
    private String organization;
    
    /**
     * Position
     */
    private String name;
    
    private String availableClasses;
    
    /**
     * Self reques approvement trait
     */
    private boolean selfApproved;
}
