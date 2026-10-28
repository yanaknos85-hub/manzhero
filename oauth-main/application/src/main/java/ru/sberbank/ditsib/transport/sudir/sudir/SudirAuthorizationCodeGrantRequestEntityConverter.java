package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.stereotype.Component;

/**
 * SUDIR access token request converter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SudirAuthorizationCodeGrantRequestEntityConverter extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        log.debug("DEBUGSUDIR: SudirAuthorizationCodeGrantRequestEntityConverter.convert called");
        return super.convert(authorizationGrantRequest);
    }
}
