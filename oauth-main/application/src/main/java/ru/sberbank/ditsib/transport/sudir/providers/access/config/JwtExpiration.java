package ru.sberbank.ditsib.transport.sudir.providers.access.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Data about JWT expiration.
 */
@Setter
@Getter
public class JwtExpiration {
    
    /**
     * Seconds to expire.
     */
    private int seconds;
    
    /**
     * Minutes to expire.
     */
    private int minutes;
    
    /**
     * Hours to expire.
     */
    private int hours;
    
    /**
     * Days to expire.
     */
    private int days;
    
    /**
     * Months to expire.
     */
    private int months;
    
    /**
     * Years to expire.
     */
    private int years;
    
}
