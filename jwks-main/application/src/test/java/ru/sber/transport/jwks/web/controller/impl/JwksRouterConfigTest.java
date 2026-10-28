package ru.sber.transport.jwks.web.controller.impl;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.Base64URL;
import io.qameta.allure.Feature;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.jwks.web.controller.JwksController;
import ru.sber.transport.jwks.web.providers.JwksProvider;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_jwks")
@DisplayName("Проверка роутера JWKS")
class JwksRouterConfigTest {

    private final JwksProvider jwksProvider = mock(JwksProvider.class);

    private final JwksController jwksController = new JwksControllerImpl(jwksProvider);

    @Test
    @DisplayName("Получение публичных ключей")
    void test_getPublicJwks() throws Exception {
        var keyGen = KeyPairGenerator.getInstance("RSA");
        var keyPair1 = keyGen.generateKeyPair();
        var publicKey1 = (RSAPublicKey) keyPair1.getPublic();
        var privateKey1 = (RSAPrivateKey) keyPair1.getPrivate();
        var keyPair2 = keyGen.generateKeyPair();
        var publicKey2 = (RSAPublicKey) keyPair2.getPublic();
        var privateKey2 = (RSAPrivateKey) keyPair2.getPrivate();

        var keys = new ArrayList<JWK>();
        keys.add(new RSAKey.Builder(publicKey1).keyUse(KeyUse.SIGNATURE).algorithm(Algorithm.parse("RS512")).keyID("key1")
            .keyOperations(Set.of(KeyOperation.VERIFY))
            .privateKey(privateKey1)
            .x509CertSHA256Thumbprint(Base64URL.encode(DigestUtils.sha256Hex(publicKey1.getEncoded())))
            .build());
        keys.add(new RSAKey.Builder(publicKey2).keyUse(KeyUse.SIGNATURE).algorithm(Algorithm.parse("RS512")).keyID("key1")
            .keyOperations(Set.of(KeyOperation.VERIFY))
            .privateKey(privateKey2)
            .x509CertSHA256Thumbprint(Base64URL.encode(DigestUtils.sha256Hex(publicKey2.getEncoded())))
            .build());

        var jwkSet = new JWKSet(keys);

        when(jwksProvider.get()).thenReturn(jwkSet);

        var mockMvc = MockMvcBuilders.standaloneSetup(jwksController).build();

        mockMvc.perform(get("/"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.keys.length()").value(2),
                jsonPath("$.keys.[0].kty").value("RSA"),
                jsonPath("$.keys.[0].use").value("sig"),
                jsonPath("$.keys.[0].key_ops.[0]").value("verify"),
                jsonPath("$.keys.[0].alg").value("RS512"),
                jsonPath("$.keys.[1].kty").value("RSA"),
                jsonPath("$.keys.[1].use").value("sig"),
                jsonPath("$.keys.[1].key_ops.[0]").value("verify"),
                jsonPath("$.keys.[1].alg").value("RS512")
            );
    }

}