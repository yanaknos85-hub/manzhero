package ru.sberbank.ditsib.transport.sudir.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sberbank.ditsib.transport.sudir.web.model.Role;
import ru.sberbank.ditsib.transport.sudir.web.model.Token;

import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;

/**
 * Controller for users authentications.
 */
@RequestMapping
public interface AuthenticationController {
    
    /**
     * Login.
     *
     * @param credentials credentials to login.
     * @param userAgent client user agent.
     * @param clientType client type.
     *
     * @return token.
     * @throws InvalidKeySpecException wrong spec of key.
     */
    @PostMapping(value = {"login", "login/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @NoAuthorize
    @ResponseBody
    @Operation(summary = "Вход", description = "Вход в систему", security = @SecurityRequirement(name = "login"))
    Token login(
            @Parameter(hidden = true) @RequestHeader("Authorization") String credentials,
            @Parameter(hidden = true) @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "x-client-type", required = false) String clientType
               ) throws InvalidKeySpecException;
    
    /**
     * Logout.
     */
    @GetMapping(value = {"logout", "logout/"})
    @NoAuthorize
    @Parameter(name = "Authorization", hidden = true)
    void logout();
    
    /**
     * Получение списка ролей.
     *
     * @param userId userId.
     * @return list of roles.
     */
    @GetMapping(value = {"roles/{userId}", "roles/{userId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    List<Role> getUserRoles(@PathVariable("userId") UUID userId);
    
}
