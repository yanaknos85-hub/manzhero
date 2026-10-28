package ru.sberbank.ditsib.transport.sudir.providers.authorization.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.JSON;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.sber.transport.database.sudir.tables.records.AuthorizationRequestRecord;
import ru.sberbank.ditsib.transport.sudir.providers.authorization.request.dao.AuthorizationRequestRepository;
import ru.sberbank.ditsib.transport.sudir.sudir.CookieConstants;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.DatabaseOAuth2AuthorizationRequestProvider;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Component
public class DatabaseOAuth2AuthorizationRequestProviderImpl implements DatabaseOAuth2AuthorizationRequestProvider {

    private final ObjectMapper objectMapper;
    private final AuthorizationRequestRepository authorizationRequestRepository;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(@NonNull HttpServletRequest request) {
        log.debug("Load authorization request from DB");

        var stateParameter = request.getParameter(OAuth2ParameterNames.STATE);
        if (stateParameter == null) {
            log.debug("State parameter is null");
            return null;
        }

        var authorizationRequest = getFromDb(request);
        if (authorizationRequest == null) {
            log.debug("Authorization request is null");
        }

        return (authorizationRequest != null && stateParameter.equals(authorizationRequest.getState()))
                ? authorizationRequest : null;
    }

    @SneakyThrows(JsonProcessingException.class)
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        log.debug("Save authorization request to DB");

        if (authorizationRequest == null) {
            log.debug("Authorization request is null");
            removeAuthorizationRequest(request, response);
            return;
        }

        var state = authorizationRequest.getState();
        Assert.hasText(state, "authorizationRequest.state cannot be empty");

        var jsonAuthRequest = objectMapper.writeValueAsString(authorizationRequest);
        var authorizationRequestRecord = new AuthorizationRequestRecord(UUID.randomUUID(), JSON.json(jsonAuthRequest));
        authorizationRequestRepository.save(authorizationRequestRecord);
        var cookie = new Cookie(CookieConstants.AUTHORIZATION_SESSION_ID, authorizationRequestRecord.getId().toString());
        var header = request.getHeader("x-envoy-original-path").split("/");
        var path = "/%s/%s/login/oauth2/code/sudir".formatted(header[1], header[2]);
        cookie.setPath(path);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, @NonNull HttpServletResponse response) {
        log.debug("Remove authorization request from DB");

        var authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            log.debug("Authorization request found");
            var deletingCookie = new Cookie(CookieConstants.AUTHORIZATION_SESSION_ID, null);
            deletingCookie.setHttpOnly(true);
            deletingCookie.setMaxAge(0);
            response.addCookie(deletingCookie);
            removeFromDb(request);
        }
        return authorizationRequest;
    }

    private void removeFromDb(HttpServletRequest request) {
        Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .parallel()
                .filter(cookie -> cookie.getName().equals(CookieConstants.AUTHORIZATION_SESSION_ID))
                .map(Cookie::getValue)
                .findAny()
                .map(UUID::fromString)
                .ifPresent(authorizationRequestRepository::deleteById);
    }

    private OAuth2AuthorizationRequest getFromDb(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .parallel()
                .filter(cookie -> cookie.getName().equals(CookieConstants.AUTHORIZATION_SESSION_ID))
                .map(Cookie::getValue)
                .findAny()
                .map(UUID::fromString)
                .map(authorizationRequestRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(AuthorizationRequestRecord::getAuthorizationRequestBody)
                .map(JSON::toString)
                .map(this::convertStringToMap)
                .map(this::parseAuthorizationRequest)
                .orElse(null);
    }

    @SneakyThrows(JsonProcessingException.class)
    private Map<String, Object> convertStringToMap(String string) {
        return objectMapper.readValue(string, new TypeReference<>() {
        });
    }

    private OAuth2AuthorizationRequest parseAuthorizationRequest(Map<String, Object> authorizationRequest) {
        return OAuth2AuthorizationRequest
                .authorizationCode()
                .authorizationRequestUri(getOrElseThrow(authorizationRequest, "authorizationRequestUri"))
                .authorizationUri(getOrElseThrow(authorizationRequest, "authorizationUri"))
                .clientId(getOrElseThrow(authorizationRequest, "clientId"))
                .redirectUri(getOrElseThrow(authorizationRequest, "redirectUri"))
                .scopes(new HashSet<>(ReflectionUtils.castObjectToList(authorizationRequest.get("scopes"), String.class)))
                .state(getOrElseThrow(authorizationRequest, "state"))
                .additionalParameters(ReflectionUtils.castObjectToMap(authorizationRequest.get("additionalParameters"), String.class, Object.class))
                .attributes(ReflectionUtils.castObjectToMap(authorizationRequest.get("attributes"), String.class, Object.class))
                .build();
    }

    private String getOrElseThrow(Map<String, Object> authorizationRequest, String key) {
        return Optional.ofNullable(authorizationRequest.get(key))
                .map(String::valueOf)
                .orElseThrow();
    }
}
