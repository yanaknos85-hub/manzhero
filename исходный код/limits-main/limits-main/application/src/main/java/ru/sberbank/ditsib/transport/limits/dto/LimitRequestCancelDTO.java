package ru.sberbank.ditsib.transport.limits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Object with data of client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitRequestCancelDTO {
    
    /**
     * ID of response
     */
    private UUID requestId;
    
    /**
     * Description of client data
     */
    private String description;
}
