package ru.sberbank.ditsib.transport.sudir.providers.authorization.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import jakarta.servlet.http.Cookie;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.records.AuthorizationRequestRecord;
import ru.sberbank.ditsib.transport.sudir.providers.authorization.request.dao.AuthorizationRequestRepository;
import ru.sberbank.ditsib.transport.sudir.sudir.CookieConstants;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.DatabaseOAuth2AuthorizationRequestProvider;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка реализации AuthorizationRequestRepository для работы с бд")
class DatabaseOAuth2AuthorizationRequestProviderImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AuthorizationRequestRepository authorizationRequestRepository =
            mock(AuthorizationRequestRepository.class);

    private final DatabaseOAuth2AuthorizationRequestProvider provider =
            new DatabaseOAuth2AuthorizationRequestProviderImpl(objectMapper, authorizationRequestRepository);

    @Test
    @DisplayName("Получение. Запрос null")
    void test_loadAuthorizationRequest_requestNull() {
        assertThrows(NullPointerException.class, () -> provider.loadAuthorizationRequest(null));
    }

    @Test
    @DisplayName("Получение. State is null")
    void test_loadAuthorizationRequest_stateNull() {
        var request = new MockHttpServletRequest(null);
        var result = provider.loadAuthorizationRequest(request);

        assertNull(result);
    }

    @Test
    @DisplayName("Получение. Нет куки")
    void test_loadAuthorizationRequest_noCookie() {
        var request = new MockHttpServletRequest(null);
        request.setParameter(OAuth2ParameterNames.STATE, "state");

        var result = provider.loadAuthorizationRequest(request);

        assertNull(result);
    }

    @Test
    @DisplayName("Получение")
    void test_loadAuthorizationRequest() throws JsonProcessingException {
        var request = new MockHttpServletRequest(null);
        request.setParameter(OAuth2ParameterNames.STATE, "state");
        request.setCookies(new Cookie(CookieConstants.AUTHORIZATION_SESSION_ID, UUID.randomUUID().toString()));

        var oauth2 = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("toster-uri")
                .clientId("toster-client")
                .state("state")
                .redirectUri("redirect-uri")
                .build();

        var oauth2String = objectMapper.writeValueAsString(oauth2);

        var authorizationRequest = new AuthorizationRequestRecord(UUID.randomUUID(), JSON.json(oauth2String));
        when(authorizationRequestRepository.findById(any())).thenReturn(Optional.of(authorizationRequest));

        var result = provider.loadAuthorizationRequest(request);
        assertEquals(oauth2.getAuthorizationUri(), result.getAuthorizationUri());
        assertEquals(oauth2.getGrantType(), result.getGrantType());
        assertEquals(oauth2.getResponseType(), result.getResponseType());
        assertEquals(oauth2.getClientId(), result.getClientId());
        assertEquals(oauth2.getRedirectUri(), result.getRedirectUri());
        assertEquals(oauth2.getScopes(), result.getScopes());
        assertEquals(oauth2.getState(), result.getState());
        assertEquals(oauth2.getAdditionalParameters(), result.getAdditionalParameters());
        assertEquals(oauth2.getAttributes(), result.getAttributes());
        assertEquals(oauth2.getAuthorizationRequestUri(), result.getAuthorizationRequestUri());
    }

    @Test
    @DisplayName("Сохранение. Запрос нул")
    void test_saveAuthorizationRequest_nullRequest() {
        assertThrows(NullPointerException.class, () -> provider.saveAuthorizationRequest(null, null, null));
    }

    @Test
    @DisplayName("Сохранение. Ответ нул")
    void test_saveAuthorizationRequest_nullResponse() {
        var request = new MockHttpServletRequest(null);
        assertThrows(NullPointerException.class, () -> provider.saveAuthorizationRequest(null, request, null));
    }

    @Test
    @DisplayName("Сохранение. Объект нул")
    void test_saveAuthorizationRequest_authRequestNull() throws JsonProcessingException {
        var uuid = UUID.randomUUID();
        var request = new MockHttpServletRequest(null);
        request.setParameter(OAuth2ParameterNames.STATE, "state");
        request.setCookies(new Cookie(CookieConstants.AUTHORIZATION_SESSION_ID, uuid.toString()));

        var oauth2 = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("toster-uri")
                .clientId("toster-client")
                .state("state")
                .redirectUri("redirect-uri")
                .build();

        var oauth2String = objectMapper.writeValueAsString(oauth2);

        var authorizationRequest = new AuthorizationRequestRecord(UUID.randomUUID(), JSON.json(oauth2String));
        when(authorizationRequestRepository.findById(any())).thenReturn(Optional.of(authorizationRequest));

        var response = new MockHttpServletResponse();

        provider.saveAuthorizationRequest(null, request, response);

        var cookie = response.getCookie(CookieConstants.AUTHORIZATION_SESSION_ID);
        assertNotNull(cookie);
        assertNull(cookie.getValue());

        var captor = ArgumentCaptor.forClass(UUID.class);
        verify(authorizationRequestRepository).deleteById(captor.capture());
        var actual = captor.getValue();
        assertEquals(uuid, actual);
    }

    @Test
    @DisplayName("Сохранение. State нул")
    void test_saveAuthorizationRequest_stateNull() {
        var request = new MockHttpServletRequest(null);
        var response = new MockHttpServletResponse();
        var oauth2 = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("toster-uri")
                .clientId("toster-client")
                .build();

        assertThrows(IllegalArgumentException.class, () -> provider.saveAuthorizationRequest(oauth2, request, response));
    }

    @Test
    @DisplayName("Сохранение")
    void test_saveAuthorizationRequest() throws JsonProcessingException {
        var request = new MockHttpServletRequest(null);
        request.addHeader("x-envoy-original-path", "/1/2/3");
        var response = new MockHttpServletResponse();
        var oauth2 = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("toster-uri")
                .clientId("toster-client")
                .state("state")
                .build();

        var jsonAuthRequest = objectMapper.writeValueAsString(oauth2);

        provider.saveAuthorizationRequest(oauth2, request, response);

        var captor = ArgumentCaptor.forClass(AuthorizationRequestRecord.class);
        verify(authorizationRequestRepository).save(captor.capture());
        var actual = captor.getValue();
        assertEquals(jsonAuthRequest, actual.getAuthorizationRequestBody().data());

        var cookie = response.getCookie(CookieConstants.AUTHORIZATION_SESSION_ID);
        assertNotNull(cookie);
        assertEquals(actual.getId().toString(), cookie.getValue());
    }

    @Test
    @DisplayName("Удаление. Ответ null")
    void test_deleteAuthorizationRequest_responseNull() {
        assertThrows(NullPointerException.class, () -> provider.removeAuthorizationRequest(null, null));
    }

    @Test
    @DisplayName("Удаление. Auth request null")
    void test_deleteAuthorizationRequest_authRequestNull() {
        var request = new MockHttpServletRequest(null);
        var response = new MockHttpServletResponse();
        var result = provider.removeAuthorizationRequest(request, response);
        assertNull(result);
    }

    @Test
    @DisplayName("Удаление")
    void test_deleteAuthorizationRequest() throws JsonProcessingException {
        var uuid = UUID.randomUUID();
        var request = new MockHttpServletRequest(null);
        request.setParameter(OAuth2ParameterNames.STATE, "state");
        request.setCookies(new Cookie(CookieConstants.AUTHORIZATION_SESSION_ID, uuid.toString()));

        var oauth2 = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("toster-uri")
                .clientId("toster-client")
                .state("state")
                .redirectUri("redirect-uri")
                .build();

        var oauth2String = objectMapper.writeValueAsString(oauth2);

        var authorizationRequest = new AuthorizationRequestRecord(UUID.randomUUID(), JSON.json(oauth2String));
        when(authorizationRequestRepository.findById(any())).thenReturn(Optional.of(authorizationRequest));

        var response = new MockHttpServletResponse();

        var result = provider.removeAuthorizationRequest(request, response);
        assertEquals(oauth2.getAuthorizationUri(), result.getAuthorizationUri());
        assertEquals(oauth2.getGrantType(), result.getGrantType());
        assertEquals(oauth2.getResponseType(), result.getResponseType());
        assertEquals(oauth2.getClientId(), result.getClientId());
        assertEquals(oauth2.getRedirectUri(), result.getRedirectUri());
        assertEquals(oauth2.getScopes(), result.getScopes());
        assertEquals(oauth2.getState(), result.getState());
        assertEquals(oauth2.getAdditionalParameters(), result.getAdditionalParameters());
        assertEquals(oauth2.getAttributes(), result.getAttributes());
        assertEquals(oauth2.getAuthorizationRequestUri(), result.getAuthorizationRequestUri());

        var cookie = response.getCookie(CookieConstants.AUTHORIZATION_SESSION_ID);
        assertNotNull(cookie);
        assertNull(cookie.getValue());

        var captor = ArgumentCaptor.forClass(UUID.class);
        verify(authorizationRequestRepository).deleteById(captor.capture());
        var actual = captor.getValue();
        assertEquals(uuid, actual);
    }
}
