package ru.sberbank.ditsib.transport.sudir.business.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * УЗ.
 */
@Data
@Builder
public class Account {
    
    /**
     * Идентификатор.
     */
    private final UUID id;
    
    /**
     * User Personnel number
     */
    private String personnelNumber;
    
    /**
     * Список ролей.
     */
    @Builder.Default
    private final Map<String, Boolean> roles = new HashMap<>();
    
    private boolean active;

    private String scope;
}
