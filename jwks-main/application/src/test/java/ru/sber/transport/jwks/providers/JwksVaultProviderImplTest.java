package ru.sber.transport.jwks.providers;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.jwks.web.providers.JwksProvider;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_jwks")
@DisplayName("Проверка JWKS провайдера Vault")
class JwksVaultProviderImplTest {

    private final VaultTemplate vaultTemplate = mock(VaultTemplate.class);

    private final JwksProvider jwksProvider = new JwksVaultProviderImpl(vaultTemplate);

    @Test
    @DisplayName("Получение")
    void test_get() {
        ((JwksVaultProviderImpl) jwksProvider).setPath("path/to/jwt");
        ((JwksVaultProviderImpl) jwksProvider).setPropertyName("property");
        ((JwksVaultProviderImpl) jwksProvider).setKid("jwt");
        ((JwksVaultProviderImpl) jwksProvider).setAlgo("RS512");

        var response = new VaultResponse();
        response.setData(Map.of("property", "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA0B72WAxz4h6l27o+8J2bUbtAIQ4ekrlxJK0j8DU5RsrBkFt42kucxgb/d53rD7wQcNyztFrbeneCln/89bAMWi4+nvjJbUFubxhYK8n+QgXQnxJ3yz6MykiUqxwrucz6Ppq+LSb1JBzcQOejn3drZ56Rn9lnkahXczhYNPKZcgFsdMJ3t6L4Wb353jFI2GNsOjaTQMNsF1bv5wjNt8YMdKhzT04oinl53D8HrdmPjRD4aDzlljNpoyRW8XaAyhqasmVb4LK5btG9ZyDTpjoXswH2kwqmx0rd/RCrtB7PGMFmt2fLD6ibWSHZaf4/BdSQf61G6xYzOCAz6GxPyTMVK0tQIb2APiZDnsi+N9idiz5hbMd8e/elE6aVyg0FbCPZ+1DkO2ZSAbKTLJaHr0A3czZgWt8K+pqTaEPNVbUsVBrFBnxd4aAZSB9FBZ6gTvRO+vjNPXP/T2s/v7bc3Lb2ZOsjaySwVApN664kLR/W53d+QEU4Zft2y8KytrQGSUs84PZprg6FZfpZRuHfwwNW27JP6bJJM8i6qhfOdHAhVW8MGl7CNoy2rJMOzP2s8+CbLxO2feAQklGUTmD1X8bdhhHCRUfp16vFBeDdQ8IeFv8lAZgMF7QqjfX2UttPAk3XkB+C9t1PyiuhUnK+8RBVbHus+2nz04mIFq/1QCYM8FsCAwEAAQ==")); // Значение из файла. Из секмана приходит именно в таком виде

        when(vaultTemplate.read("path/to/jwt")).thenReturn(response);

        var actual = jwksProvider.get();

        assertThat(actual.getKeys()).hasSize(1);
        assertThat(actual.getKeyByKeyId("jwt")).isNotNull();
        assertThat(actual.getKeyByKeyId("jwt").getAlgorithm()).isEqualTo(Algorithm.parse("RS512"));
        assertThat(actual.getKeyByKeyId("jwt").getKeyID()).isEqualTo("jwt");
        assertThat(actual.getKeyByKeyId("jwt").getKeyStore()).isNull();
        assertThat(actual.getKeyByKeyId("jwt").getKeyOperations()).hasSameElementsAs(Set.of(KeyOperation.VERIFY));
        assertThat(actual.getKeyByKeyId("jwt").getKeyType()).isEqualTo(KeyType.RSA);
        assertThat(actual.getKeyByKeyId("jwt").getKeyUse()).isEqualTo(KeyUse.SIGNATURE);
    }

}