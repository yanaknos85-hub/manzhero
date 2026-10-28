package ru.sberbank.ditsib.transport.sudir.providers.access.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties of JWT.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * Issuer of token.
     */
    private String issuer;
    
    /**
     * Expiration of token.
     */
    private JwtExpiration expire = new JwtExpiration();
    
}
