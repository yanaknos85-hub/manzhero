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
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.fake.DoNotStartThis;
import ru.sber.transport.authorization.model.ConsentCheckModel;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IsolatedTest
@UnitTest
@Feature("lib_authorization")
@SpringBootTest(classes = DoNotStartThis.class,
        properties = {
                "jwt.key.public.path=classpath:/key/jwt.key.pub",
                "jwt.key.private.path=classpath:/key/jwt.key",
                "test.user=spelUser",
                "test.pass=spelPass",
                "test.role=ROLE_SPEL",
                "authorization.consent-check=true"
        })
@DisplayName("Проверка доступа по ПДн")
@AutoConfigureMockMvc
@TestPropertySource(properties = "classpath:/application.yml")
public class ConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockitoBean
    private ConsentFunction consentFunction;

    @MockitoBean
    private AuthorizationManager<RequestAuthorizationContext> accessManager;

    @Test
    @DisplayName("Вызов ручки с ПДн подписавшим пользователем")
    void test_pdn_signed() throws Exception {
        var consent = true;
        var token = createWhen();

        when(consentFunction.apply(any(ConsentCheckModel.class))).thenReturn(consent);

        mockMvc.perform(get("/consent/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("Вызов ручки с ПДн подписавшим пользователем водителем")
    void test_pdn_signed_driver() throws Exception {
        var consent = true;
        var token = createWhen();

        when(consentFunction.apply(any(ConsentCheckModel.class))).thenReturn(consent);

        mockMvc.perform(get("/consent/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("Вызов ручки с ПДн НЕ подписавшим пользователем")
    void test_pdn_not_signed() throws Exception {
        var consent = false;
        var token = createWhen();

        when(consentFunction.apply(any(ConsentCheckModel.class))).thenReturn(consent);

        mockMvc.perform(get("/consent/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Вызов ручки БЕЗ ПДн подписавшим пользователем")
    void test_without_pdn_signed() throws Exception {
        var consent = true;
        var token = createWhen();

        when(consentFunction.apply(any(ConsentCheckModel.class))).thenReturn(consent);

        mockMvc.perform(get("/consent-skip/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("Вызов ручки БЕЗ ПДн НЕ подписавшим пользователем")
    void test_without_pdn_not_signed() throws Exception {
        var consent = false;
        var token = createWhen();

        when(consentFunction.apply(any(ConsentCheckModel.class))).thenReturn(consent);

        mockMvc.perform(get("/consent-skip/path/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("Вызов ручки, у которой проверка отменена на уровне класса")
    void test_controllerSkip() throws Exception {
        mockMvc.perform(get("/controllerSkip/"))
                .andExpect(status().isOk())
                .andExpect(content().string("alive"));
    }

    @Test
    @DisplayName("Вызов ручки, у которой проверка отменена в конфигурации")
    void test_skipUrlInConfig() throws Exception {
        mockMvc.perform(get("/skipUrlInConfig/"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    private String createWhen() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
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

        return generateToken(claims);
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
        var key = new RSAKey(
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
}
