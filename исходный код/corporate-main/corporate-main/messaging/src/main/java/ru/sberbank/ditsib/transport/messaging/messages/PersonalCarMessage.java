package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Personal auto message.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalCarMessage implements Message<UUID> {
    
    /**
     * ID of personal car.
     */
    private UUID id;
    
    /**
     * Brand name
     */
    private String brandName;
    
    /**
     * Model name
     */
    private String model;
    
    /**
     * Registration (state) number
     */
    private String registrationNumber;
    
    /**
     * Registration certificate
     */
    private String registrationCertificate;
    
    /**
     * Engine volume, cm^3
     */
    private int engineVolume;
    
    /**
     * Insurance number
     */
    private String insuranceNumber;
    
    /**
     * Corporate user id
     */
    private UUID employeeId;
    
    /**
     * Non corporate owner info
     */
    private String ownerInfo;
    
    /**
     * Flag of personal car deleted.
     */
    @Builder.Default
    private boolean deleted = false;
}
