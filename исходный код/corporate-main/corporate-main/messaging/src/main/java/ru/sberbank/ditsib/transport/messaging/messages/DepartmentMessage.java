package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Department message.
 */
@Jacksonized
@Builder
@Getter
public class DepartmentMessage implements Message<UUID> {
    
    /**
     * ID of department.
     */
    private final UUID id;
    
    /**
     * Human readable Id.
     */
    private final String humanReadableId;
    
    /**
     * id of parent organization
     */
    private final UUID organizationId;
    
    /**
     * Unique code
     */
    private final String code;
    
    /**
     * Name of department
     */
    private final String departmentName;
    
    /**
     * Id of parent department
     */
    private final UUID parentId;
    
    /**
     * Id of department head
     */
    private final UUID departmentHeadId;
    
    /**
     * Location of department
     */
    private final String location;

    /**
     * Easup ID
     */
    private final String easupId;
    
    
    /**
     * Flag of department deleted.
     */
    @Builder.Default
    private final boolean deleted = false;
    
}
