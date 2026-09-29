package ru.sber.transport.integrations.aop;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ru.sber.transport.integrations.dto.CredentialClient;
import ru.sber.transport.integrations.exception.AuthorizationException;
import ru.sber.transport.integrations.service.AuthorizationService;
import ru.sber.transport.integrations.service.CredentialService;

import java.time.Instant;
import java.util.stream.Stream;

@Aspect
@Component
@RequiredArgsConstructor
public class AutoAuthorizationAspect {
    
    private final AuthorizationService authService;
    private final CredentialService credentialService;
    
    @Before(value = "@annotation(ru.sber.transport.integrations.aop.AutoAuthorization)")
    public void autoAuth(final JoinPoint joinPoint) {
        var credentialClient = getCredentialClient(joinPoint.getArgs());
        
        if (credentialClient == null ||
            credentialClient.getUri() == null ||
            credentialClient.getLogin() == null ||
            credentialClient.getPassword() == null) {
            throw new AuthorizationException("Credential isn't specified");
        }
        
        var credential = credentialService.get(credentialClient.getUri(), credentialClient.getLogin());
        var now = Instant.now();
        
        if (credential == null ||
            StringUtils.isEmpty(credential.token()) ||
            now.isAfter(credential.expirationRefreshToken())
        ) {
            authService.auth(credentialClient);
            return;
        }
        
        if (now.isAfter(credential.expirationToken())) {
            if (now.isBefore(credential.expirationRefreshToken())) {
                authService.refresh(credentialClient);
            } else {
                authService.auth(credentialClient);
            }
        }
    }
    
    private CredentialClient getCredentialClient(Object[] args) {
        return Stream.of(args)
                     .filter(CredentialClient.class::isInstance)
                     .map(CredentialClient.class::cast)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("No value present"));
    }
    
}
