package ru.sberbank.ditsib.transport.sudir.web.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.messages.BlackListMessage;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sber.transport.database.sudir.tables.records.SessionRecord;
import ru.sber.transport.sudir.messages.AccountMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.refresh.dao.SessionRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.sudir.web.model.Token;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка аутентификации")
@ActiveProfiles("test")
class AuthenticationControllerTest {

    private static final String USER_ID = "95a9ddc6-e62d-4061-9e65-47982ec2cf4c";

    private static ClientAndServer mockServer;

    @AfterAll
    static void stopMockServer() {
        mockServer.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @MockitoSpyBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean(name = "accountsOutputSsl")
    private OutputBridge accountsOutputSsl;

    @MockitoBean(name = "blackListOutput")
    private OutputBridge blackListOutput;

    @Autowired
    @Qualifier("jwtPrivateKey")
    private Key jwtSecretKey;

    private static final KeyPair oauthToken; // Алгоритм подписи, с которым работает СУДИР.

    static {
        try {
            oauthToken = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        mockServer = ClientAndServer.startClientAndServer(59999);
        var userInfo = Map.of(
            "employeenumber", "testEmployeeNumber",
            "userName", "testEmployeeName"
        );
        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/userInfo"))
            .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON_UTF_8)
                .withBody(new ObjectMapper().writeValueAsString(userInfo)));
        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/jwk"))
            .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON_UTF_8)
                .withBody("""
                    {
                        "keys": [
                        {
                        "kty": "RSA",
                        "n": "%s",
                        "e": "%s"
                        }
                        ]
                    }
                    """.formatted(
                    Base64.getEncoder().encodeToString(((RSAPublicKey) oauthToken.getPublic()).getModulus().toByteArray()),
                    Base64.getEncoder().encodeToString(((RSAPublicKey) oauthToken.getPublic()).getPublicExponent().toByteArray())
                )));
    }

    @Test
    @DisplayName("Проверка входа с токеном обновления")
    void testLogin_refresh() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setPersonnelNumber("login");

        account = accountRepository.save(account);

        var issuedAt = LocalDateTime.now();

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setIssuedAt(issuedAt);
        session.setExpireAt(LocalDateTime.now().plusDays(1));
        session.setToken("old token");

        session = sessionRepository.save(session);

        var response =
            mockMvc.perform(post("/login").header("Authorization", "Token " + session.getId().toString()).header("User-Agent", "WEB_CORP"))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), Token.class);

        assertThat(sessionRepository.count()).isEqualTo(1);
        assertThat(sessionRepository.findAll().getFirst()).isNotEqualTo(session);
        assertThat(actual.getRefreshToken()).isNotEqualTo(session.getId().toString());
        assertThat(actual.getAccessToken()).isNotEqualTo(session.getToken());

        var messageCaptor = ArgumentCaptor.forClass(AccountMessage.class);
        verify(accountsOutputSsl).send(messageCaptor.capture());
        var accountMessage = messageCaptor.getValue();

        assertThat(accountMessage.getId()).isEqualTo(account.getId().toString());
        assertThat(accountMessage.lastLoginDate().toLocalDate()).isEqualTo(sessionRepository.findAll().getFirst().getIssuedAt().toLocalDate());

        var blackListMessageArgumentCaptor = ArgumentCaptor.forClass(BlackListMessage.class);
        verify(blackListOutput).send(blackListMessageArgumentCaptor.capture());
        var message = blackListMessageArgumentCaptor.getValue();
        assertThat(message).isNotNull();

        assertThat(message.getToken()).isEqualTo("old token");
    }

    @Test
    @DisplayName("Проверка входа с истекшим токеном обновления")
    void testLogin_refresh_expired() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setPersonnelNumber("login");

        account = accountRepository.save(account);

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setExpireAt(LocalDateTime.now().minusMonths(1));
        session.setToken("old token");

        session = sessionRepository.save(session);

        var result = mockMvc.perform(post("/login")
                .header("Authorization", "Token " + session.getId().toString()).header("User-Agent", "WEB_CORP"))
            .andExpect(status().isUnauthorized());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Проверка входа с неверным токеном обновления")
    void testLogin_refresh_wrongToken() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setPersonnelNumber("login");

        account = accountRepository.save(account);

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setAccountId(account.getId());
        session.setExpireAt(LocalDateTime.now().plusDays(1));
        session.setToken("old token");

        sessionRepository.save(session);

        var token = UUID.randomUUID().toString();
        mockMvc.perform(post("/login").header("Authorization", "Token " + token).header("User-Agent", "WEB_CORP"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Выход")
    void logout() throws Exception {
        var account = new AccountRecord();
        account.setId(UUID.fromString(USER_ID));
        account.setPersonnelNumber("login");

        account = accountRepository.save(account);

        var header = JwsHeader.with(SignatureAlgorithm.RS512).type("JWT").build();
        var claims = JwtClaimsSet.builder()
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .issuedAt(Instant.now())
            .issuer("Sbertransport")
            .subject(account.getPersonnelNumber())
            .id(account.getId().toString())
            .claim("roles", Arrays.asList("ROLE_1", "ROLE_2"))
            .build();
        var parameters = JwtEncoderParameters.from(header, claims);
        var jwk = new RSAKey((RSAPublicKey) oauthToken.getPublic(), (PrivateKey) jwtSecretKey, KeyUse.SIGNATURE, Set.of(KeyOperation.SIGN), Algorithm.parse("RS512"), "jwk", null, null, null, null, null, null, null, null);
        var jwkSet = new JWKSet(jwk);
        var jwks = new ImmutableJWKSet<>(jwkSet);
        var jwt = new NimbusJwtEncoder(jwks).encode(parameters);
        var token = jwt.getTokenValue();

        var session = new SessionRecord();
        session.setId(UUID.randomUUID());
        session.setExpireAt(LocalDateTime.now().plusDays(1));
        session.setToken(token);
        session.setAccountId(account.getId());

        sessionRepository.save(session);

        assertThat(sessionRepository.count()).isEqualTo(1);

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        mockMvc.perform(get("/logout").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        assertThat(sessionRepository.count()).isZero();

        var blackListMessageArgumentCaptor = ArgumentCaptor.forClass(BlackListMessage.class);
        verify(blackListOutput).send(blackListMessageArgumentCaptor.capture());
        var message = blackListMessageArgumentCaptor.getValue();
        assertThat(message).isNotNull();

        assertThat(message.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("Выход. Не входил")
    void logout_noEntered() throws Exception {
        mockMvc.perform(get("/logout")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Вход. Судир не отвечает")
    void login_sudir_not_responded() throws Exception {
        var result = mockMvc.perform(get("/login/oauth2/code/sudir"))
            .andExpect(status().isFound())
            .andReturn();

        var redirect = result.getResponse().getRedirectedUrl();

        assertThat(redirect).isNotNull();

        mockMvc.perform(get(redirect))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.path").value("/failed"));
    }

    @Test
    @DisplayName("Вход. Судир не принял запрос")
    void login_sudir_not_accepted() throws Exception {
        var result = mockMvc.perform(get("/login/oauth2/code/sudir"))
            .andExpect(status().isFound())
            .andReturn();

        var redirect = result.getResponse().getRedirectedUrl();

        assertThat(redirect).isNotNull();

        var redirectParts = redirect.split("&");
        var redirectUri = redirectParts[redirectParts.length - 1].replace("redirect_uri=", "");

        mockMvc.perform(get(redirectUri))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.path").value("/failed"))
        ;
    }

    @Test
    @DisplayName("Вход. Запрос токена провалился")
    void login_sudir_token_request_failed() throws Exception {
        var result = mockMvc.perform(get("/login/oauth2/code/sudir"))
            .andExpect(status().isFound())
            .andReturn();

        var redirect = result.getResponse().getRedirectedUrl();
        assertThat(redirect).isNotNull();

        var redirectParts = redirect.split("&");
        var redirectUri = redirectParts[redirectParts.length - 1].replace("redirect_uri=", "");

        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/token/1"))
            .respond(HttpResponse.notFoundResponse());

        mockMvc.perform(get("%s?code=success&state=success".formatted(redirectUri))
                .sessionAttr("org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository" +
                    ".AUTHORIZATION_REQUEST", createRequest("1")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.path").value("/failed"));
    }

    @Test
    @DisplayName("Вход. Нет табельника")
    @Disabled("Требуется переработка")
    void login_sudir_not_personnelNumber() throws Exception {
        var result = mockMvc.perform(get("/login/oauth2/code/sudir"))
            .andExpect(status().isFound())
            .andReturn();

        var redirect = result.getResponse().getRedirectedUrl();

        assertThat(redirect).isNotNull();

        var redirectParts = redirect.split("&");
        var redirectUri = redirectParts[redirectParts.length - 1].replace("redirect_uri=", "");

        var personnelNumber = "testPersonnelNumber";

        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/token/2"))
            .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON_UTF_8).withBody(createResponse(personnelNumber)));

        mockMvc.perform(get("%s?code=success&state=success".formatted(redirectUri))
                .sessionAttr("org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository" +
                    ".AUTHORIZATION_REQUEST", createRequest("2")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.path").value("/failed#personnelNumber=%s".formatted(personnelNumber)))
        ;
    }

    @Test
    @DisplayName("Вход")
    @Disabled("Требуется переработка")
    void login_sudir() throws Exception {
        var result = mockMvc.perform(get("/login/oauth2/code/sudir"))
            .andExpect(status().isFound())
            .andReturn();

        var redirect = result.getResponse().getRedirectedUrl();
        assertThat(redirect).isNotNull();

        var redirectParts = redirect.split("&");
        var redirectUri = redirectParts[redirectParts.length - 1].replace("redirect_uri=", "");

        var personnelNumber = "testPersonnelNumber";

        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/token/3"))
            .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON_UTF_8).withBody(createResponse(personnelNumber)));

        var role1 = roleRepository.save(createRole("ROLE", "Name"));
        var role2 = roleRepository.save(createRole("ROLE2", "Name2"));

        var account = new AccountRecord();
        account.setPersonnelNumber(personnelNumber);
        account.setActive(true);
        account.setId(UUID.randomUUID());

        accountRepository.save(account);
        roleRepository.addRole(account, role1);
        roleRepository.addRole(account, role2);

        var response = mockMvc.perform(get("%s?code=success&state=success".formatted(redirectUri))
                .sessionAttr("org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository" +
                    ".AUTHORIZATION_REQUEST", createRequest("3")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.path").value("/failed#personnelNumber=%s".formatted(personnelNumber)));

        assertThat(sessionRepository.count()).isEqualTo(1);
        var session = sessionRepository.findAll().getFirst();

        response.andExpect(redirectedUrl("/success#token=%s&refreshToken=%s".formatted(session.getToken(), session.getId().toString())));

        var messageCaptor = ArgumentCaptor.forClass(AccountMessage.class);
        verify(accountsOutputSsl).send(messageCaptor.capture());
        var accountMessage = messageCaptor.getValue();
        assertThat(accountMessage.getId()).isEqualTo(account.getId().toString());
        assertThat(accountMessage.lastLoginDate().toLocalDate()).isEqualTo(sessionRepository.findAll().getFirst().getIssuedAt().toLocalDate());
    }

    private RoleRecord createRole(String code, String name) {
        var role = new RoleRecord();
        role.setCode(code);
        role.setName(name);
        return role;
    }

    private String createResponse(String personnelNumber) {
        var header = JwsHeader.with(SignatureAlgorithm.RS512).type("JWT").build();
        var claims = JwtClaimsSet.builder()
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .issuedAt(Instant.now())
            .issuer("http://localhost")
            .subject(personnelNumber)
            .id("id")
            .audience(List.of("clientId"))
            .claim("employeenumber", "testPersonnelNumber")
            .build();
        var parameters = JwtEncoderParameters.from(header, claims);
        var jwk = new RSAKey((RSAPublicKey) oauthToken.getPublic(), oauthToken.getPrivate(), KeyUse.SIGNATURE, Set.of(KeyOperation.SIGN), Algorithm.parse("RS512"), "jwk", null, null, null, null, null, null, null, null);
        var jwkSet = new JWKSet(jwk);
        var jwks = new ImmutableJWKSet<>(jwkSet);
        var token = new NimbusJwtEncoder(jwks).encode(parameters);

        return """
            {
             "access_token": "%s",
             "refresh_token": "%s",
             "%s": "%s",
             "%s": "%s"
            }
            """.formatted(token, "token", OidcParameterNames.ID_TOKEN, token, OAuth2ParameterNames.TOKEN_TYPE, "Bearer");
    }

    private OAuth2AuthorizationRequest createRequest(String tokenId) {
        var attributes = new HashMap<String, Object>();
        var regId = "regId";
        var auth = "auth";
        var redirect = "redirect";
        var clientId = "clientId";
        var scopes = Set.of("openid");
        var additionalParams = Map.<String, Object>of(
            OidcParameterNames.ID_TOKEN, "tokenId"
        );
        attributes.put(OAuth2ParameterNames.REGISTRATION_ID, regId);
        when(clientRegistrationRepository.findByRegistrationId(regId))
            .thenReturn(createClientRegistration(regId, auth, redirect, clientId, scopes, tokenId));
        return OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(auth)
            .clientId(clientId)
            .redirectUri(redirect)
            .attributes(attributes)
            .scopes(scopes)
            .additionalParameters(additionalParams)
            .state("success").build();
    }

    private ClientRegistration createClientRegistration(
        String regId, String auth, String redirect, String clientId, Set<String> scopes, String tokenId
    ) {
        return ClientRegistration.withRegistrationId(regId).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).scope(scopes)
            .clientId(clientId) // Идентификатор клиента
            .redirectUri(redirect) // Урл перенаправления после аутентификации
            .authorizationUri(auth) // Урл перенаправления на авторизацию
            .tokenUri("http://localhost:59999/token/%s".formatted(tokenId)) // Урл получения токена
            .jwkSetUri("http://localhost:59999/jwk") // Урл получения публичного ключа для проверки токена
            .userInfoUri("http://localhost:59999/userInfo") // Урл получения инфы о пользаке
            .userNameAttributeName("employeenumber") // Должен соответствовать полю с данными
            .build();
    }
}