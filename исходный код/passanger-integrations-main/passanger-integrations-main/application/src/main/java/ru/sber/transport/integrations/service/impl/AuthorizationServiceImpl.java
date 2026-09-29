package ru.sber.transport.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.config.JsonApiProperties;
import ru.sber.transport.integrations.dto.Authorization;
import ru.sber.transport.integrations.dto.CredentialClient;
import ru.sber.transport.integrations.dto.Token;
import ru.sber.transport.integrations.encryption.PasswordEncryption;
import ru.sber.transport.integrations.exception.JwtValidationException;
import ru.sber.transport.integrations.feign.AuthorizationClient;
import ru.sber.transport.integrations.service.AuthorizationService;
import ru.sber.transport.integrations.service.CredentialService;
import ru.sber.transport.integrations.service.ValidationResponseService;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {
    
    private final AuthorizationClient authApi;
    private final CredentialService credentialService;
    private final ValidationResponseService validationService;
    private final PasswordEncryption passwordEncryption;
    private final JsonApiProperties jsonApiProperties;
    
    @Override
    public void auth(CredentialClient credential) {
        log.info("Authorization, url: {}, login:{}", credential.getUri(), credential.getLogin());
        var response = authApi.auth(getRequestUri(credential),
                                    auth(credential.getLogin(), credential.getPassword()),
                                    credential.getUri().toString());
        validationService.validate(response);
        
        var auth = Optional.ofNullable(response.getBody()).orElseThrow();
        
        saveAuth(credential.getUri(), credential.getLogin(), auth);
        log.info("Authorization successful");
    }
    
    @Override
    public void refresh(CredentialClient credential) {
        log.debug("Refresh Authorization, URL: {}", credential.getUri());
        var response = authApi.auth(getRequestUri(credential), auth(credential.getLogin(), credential.getPassword()), credential.getUri().toString());
        
        validationService.validate(response);
        
        var auth = Optional.ofNullable(response.getBody()).orElseThrow();
        
        saveAuth(credential.getUri(), credential.getLogin(), auth);
        log.debug("Authorization successful");
    }
    
    private Instant getExpiration(String token) {
        try {
            var chunks = token.split("\\.");
            var decoder = Base64.getUrlDecoder();
            var payload = new String(decoder.decode(chunks[1]));
            
            var parser = JsonParserFactory.getJsonParser();
            var instant = (int) parser.parseMap(payload).get("exp");
            
            return Instant.ofEpochSecond(instant);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new JwtValidationException("Token is not valid. " + e.getMessage());
        }
    }
    
    private String auth(String login, String password) {
        return "Basic " + new String(
                Base64.getEncoder().encode(String.format("%s:%s", login, passwordEncryption.decode(password)).getBytes()));
    }
    
    private void saveAuth(URI url, String login, Authorization auth) {
        credentialService.add(url, login,
                              new Token(auth.accessToken(),
                                  getExpiration(auth.accessToken()),
                                  auth.refreshToken(),
                                  auth.refreshExpiration() != null ? auth.refreshExpiration().toInstant(ZoneOffset.UTC) : getExpiration(auth.refreshToken()),
                                  null)
                             );
    }
    
    private URI getRequestUri(CredentialClient credential) {
        if (jsonApiProperties.getRequestUrl() == null) {
            return credential.getUri();
        }
        return URI.create(jsonApiProperties.getRequestUrl());
    }
}
