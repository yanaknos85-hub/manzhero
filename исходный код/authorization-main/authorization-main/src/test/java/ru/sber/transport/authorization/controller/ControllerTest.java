package ru.sber.transport.authorization.controller;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.fake.DoNotStartThis;
import ru.sber.transport.authorization.service.BlackListService;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@SpringBootTest(classes = DoNotStartThis.class,
        properties = {
                "jwt.key.public.path=classpath:/key/jwt.key.pub",
                "jwt.key.private.path=classpath:/key/jwt.key",
                "test.user=spelUser",
                "test.pass=spelPass",
                "test.role=ROLE_SPEL"
        })
@DisplayName("Проверка авторизации")
@AutoConfigureMockMvc
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockitoBean
    private EmployeeOrganizationFunction employeeOrganizationFunction;

    @MockitoBean
    private BlackListService blackListService;

    @MockitoBean
    private AuthorizationManager<RequestAuthorizationContext> accessManager;

    @Test
    @DisplayName("Авторизация")
    void test_authorize() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_2");
        var subject = UUID.randomUUID().toString();
        var id = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(id)
                .claim("roles", Arrays.asList("ROLE_1", "ROLE_2"))
                .build();

        var token = generateToken(claims);

        mockMvc.perform(post("/login/token/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(id));
    }

    @Test
    @DisplayName("Авторизация. PATCH")
    void test_authorize_sudir() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_2");
        var subject = UUID.randomUUID().toString();
        var id = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(id)
                .claim("roles", Arrays.asList("ROLE_1", "ROLE_2"))
                .build();

        var token = generateToken(claims);

        mockMvc.perform(patch("/login/sudir/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(subject));
    }

    @Test
    @DisplayName("Авторизация. Нет авторизации")
    void test_non_authorize() throws Exception {
        mockMvc.perform(post("/login/token/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Не авторизован")
    void test_no_authorized() throws Exception {
        mockMvc.perform(put("/authorize/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Авторизован")
    void test_authorized() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", Arrays.asList("ROLE_1", "ROLE_2"))
                .build();

        var token = generateToken(claims);

        mockMvc.perform(put("/authorize/?data=data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("data"));
    }

    @Test
    @DisplayName("Авторизован. Токен заблокирован")
    void test_authorized_clocked() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", Arrays.asList("ROLE_1", "ROLE_2"))
                .build();

        var token = generateToken(claims);

        when(blackListService.check(token)).thenReturn(true);

        try {
            var result = mockMvc.perform(post("/authorize/?data=data").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
            assertThat(result).isEqualTo(401);
        } catch (Exception e) {
            // success
        }
    }

    @Test
    @DisplayName("Авторизован. Есть роль. POST")
    void test_authorized_role_post() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_2");
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", Arrays.asList("ROLE_1", "ROLE_2"))
                .build();

        var token = generateToken(claims);

        mockMvc.perform(delete("/authorize/role/?data=data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("data"));
    }

    @Test
    @DisplayName("Авторизация не требуется")
    void test_authorization_not_required() throws Exception {
        mockMvc.perform(post("/noAuthorize/?data=data"))
                .andExpect(status().isOk())
                .andExpect(content().string("data"));
    }

    @Test
    @DisplayName("Проверка доступа к организации")
    void test_checkOrganizationAccess() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var organizationId = UUID.randomUUID();
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .build();

        var token = generateToken(claims);

        when(employeeOrganizationFunction.apply(any(UUID.class))).thenReturn(organizationId);

        mockMvc.perform(get("/access/path/" + organizationId + "/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(organizationId.toString()));
    }

    @Test
    @DisplayName("Проверка доступа к СМД")
    void test_checkOrganizationAccess_smd() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");

        var organizationId = UUID.randomUUID();
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .claim("data_master", true)
                .build();

        var token = generateToken(claims);

        when(employeeOrganizationFunction.apply(any(UUID.class))).thenReturn(organizationId);

        mockMvc.perform(get("/access/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка доступа к СМД. Не СМД")
    void test_checkOrganizationAccess_no_smd() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var organizationId = UUID.randomUUID();
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .claim("data_master", false)
                .build();

        var token = generateToken(claims);

        when(employeeOrganizationFunction.apply(any(UUID.class))).thenReturn(organizationId);

        mockMvc.perform(get("/access/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Проверка доступа к организации. Данные встроены")
    void test_checkOrganizationAccess_inheritData() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var organizationId = UUID.randomUUID();
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .build();

        var token = generateToken(claims);

        when(employeeOrganizationFunction.apply(any())).thenReturn(organizationId);

        var content = """
                {
                "organizationId": "%s"
                }
                """.formatted(organizationId);

        mockMvc.perform(post("/access/field/").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(content().string(organizationId.toString()));
    }

    @Test
    @DisplayName("Проверка доступа к организации. Данные встроены. Нет доступа")
    void test_checkOrganizationAccess_inheritData_noAccess() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var organizationId = UUID.randomUUID();
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .build();

        var token = generateToken(claims);

        when(employeeOrganizationFunction.apply(any())).thenReturn(organizationId);

        var content = """
                {
                "organizationId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/access/field/").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isForbidden());
    }

    private String generateToken(JwtClaimsSet claims) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        var publicResource = resourceLoader.getResource("classpath:/key/jwt.key.pub");
        var rawPublicKey = publicResource.getContentAsString(StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
        var publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(rawPublicKey)));

        var privateResource = resourceLoader.getResource("classpath:/key/jwt.key");
        var rawPrivateKey = privateResource.getContentAsString(StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        var privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rawPrivateKey)));
        @SuppressWarnings("deprecation") var key = new RSAKey(
                (RSAPublicKey) publicKey,
                (RSAPrivateKey) privateKey,
                KeyUse.SIGNATURE,
                Set.of(KeyOperation.SIGN),
                Algorithm.parse(SignatureAlgorithm.RS512.name()), null, null, null, null, null, null);

        var header = JwsHeader.with(SignatureAlgorithm.RS512).build();
        var parameters = JwtEncoderParameters.from(header, claims);
        var jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
        return jwtEncoder.encode(parameters).getTokenValue();
    }

    @Test
    @DisplayName("Проверка доступа к организации. Нет функции")
    void test_checkOrganizationAccess_noFunction() throws Exception {
        employeeOrganizationFunction = null;
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .build();

        var token = generateToken(claims);

        var content = """
                {
                "organizationId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/access/field/").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Проверка доступа к организации. Нет доступа")
    void test_checkOrganizationAccess_noAccess() throws Exception {
        AuthorizeUtils.authorize(accessManager, "ROLE_1");
        var organizationId = UUID.randomUUID();
        var subject = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .issuedAt(Instant.now())
                .issuer("Sbertransport")
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_1"))
                .build();

        var token = generateToken(claims);

        mockMvc.perform(get("/access/path/" + organizationId + "/").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

}