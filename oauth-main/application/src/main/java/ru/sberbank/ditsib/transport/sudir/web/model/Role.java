package ru.sberbank.ditsib.transport.sudir.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Object with data about roles.
 */
@Getter
@Builder
@AllArgsConstructor
public class Role {
    
    /**
     * Code.
     */
    private final String code;
    
    /**
     * Code.
     */
    private final String name;
    
    /**
     * Code.
     */
    private final String description;
    
    /**
     * Flag of default role.
     */
    @Builder.Default
    private final List<String> scopes = new ArrayList<>();
    
}
