package ru.sber.transport.authorization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.model.ConsentCheckModel;
import ru.sber.transport.authorization.service.CheckConsentAccessService;
import ru.sber.transport.authorization.service.ConsentFunction;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class CheckConsentAccessServiceImpl implements CheckConsentAccessService {

    private final List<ConsentFunction> consentFunctions;

    public boolean check() throws UnauthorizedException {
        var token = getToken();
        ArrayList<String> roles = new ArrayList<>();
        token.ifPresent(jwt -> {
            if (jwt.hasClaim("roles")) {
                roles.addAll(jwt.getClaimAsStringList("roles"));
            }
        });
        var model = new ConsentCheckModel(getAuthenticatedUser(), roles);
        for (var function : consentFunctions) {
            var result = function.apply(model);
            if (result != null && result) {
                return true;
            }
        }
        log.error("Can not access endpoints without consent");
        return false;
    }

    /**
     * Получение данных аутентифицированного пользователя.
     *
     * @return идентификатор пользователя.
     */
    private UUID getAuthenticatedUser() {
        var authentication = getAuthentication();
        return Optional.ofNullable(authentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getId)
                .map(UUID::fromString).orElse(null);
    }

    /**
     * Получение данных аутентификации.
     *
     * @return данные аутентификации.
     */
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Получение токена пользователя.
     *
     * @return токен.
     */
    private Optional<Jwt> getToken() {
        var authentication = getAuthentication();
        return Optional.ofNullable(authentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getToken);
    }
}
