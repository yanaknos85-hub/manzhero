package ru.sberbank.ditsib.corpclient.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Helper for less boiler plate code
 */
@UtilityClass
public class ContextHelper {
    /**
     * get current user from Spring context
     *
     * @return sting with current user id
     */
    public static String getCurrentUser() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token
        ) {
            return token.getToken().getId();
        }
        return null;
    }
}
