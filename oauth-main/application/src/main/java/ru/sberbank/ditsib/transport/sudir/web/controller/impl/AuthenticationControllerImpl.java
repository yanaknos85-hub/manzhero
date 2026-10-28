package ru.sberbank.ditsib.transport.sudir.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.AccountNotFoundException;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.RefreshTokenExpired;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.web.controller.AuthenticationController;
import ru.sberbank.ditsib.transport.sudir.web.exceptions.LoginException;
import ru.sberbank.ditsib.transport.sudir.web.exceptions.UnknownAuthenticationTypeException;
import ru.sberbank.ditsib.transport.sudir.web.exceptions.WrongAuthenticationDataException;
import ru.sberbank.ditsib.transport.sudir.web.mappers.RoleMapper;
import ru.sberbank.ditsib.transport.sudir.web.mappers.TokenMapper;
import ru.sberbank.ditsib.transport.sudir.web.model.Role;
import ru.sberbank.ditsib.transport.sudir.web.model.Token;

import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
class AuthenticationControllerImpl implements AuthenticationController {
    
    private final Accounts accounts;
    
    private final TokenMapper tokenMapper;
    
    private final RoleMapper roleMapper;
    
    @Override
    public Token login(String credentials, String userAgent, String clientType) throws InvalidKeySpecException {
        var credentialsParts = credentials.split(" ");
        if (credentialsParts.length != 2) {
            throw new WrongAuthenticationDataException(credentials);
        }
        var type = credentialsParts[0];
        var credentialsValue = credentialsParts[1];
        if ("Token".equals(type)) {
            return refreshAuthentication(credentialsValue, userAgent, clientType);
        }
        throw new UnknownAuthenticationTypeException(type);
    }
    
    @Override
    public void logout() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            var jwt = token.getToken();
            var userId = UUID.fromString(jwt.getId());
            accounts.logout(userId, jwt.getTokenValue());
        }
    }

    @SuppressWarnings("java:S3958")
    @Override
    @SneakyThrows(AccountNotFoundException.class)
    public List<Role> getUserRoles(@PathVariable("userId") UUID userId) {
        return accounts.getRoles(userId).stream().map(roleMapper::toModel).toList();
    }
    
    /**
     * Аутентификация по токену обновления.
     *
     * @param credentialsValue креды.
     * @return токен в случае успешной аутентификации.
     */
    private Token refreshAuthentication(String credentialsValue, String userAgent, String clientType) throws InvalidKeySpecException {
        var refresh = credentialsValue.replace("Token ", "");
        try {
            var newToken = accounts.relogin(refresh, userAgent, clientType);
            return tokenMapper.toDto(newToken);
        } catch (RefreshTokenExpired refreshTokenExpired) {
            throw new LoginException(LoginException.Type.REFRESH_EXPIRED);
        }
    }
    
}
