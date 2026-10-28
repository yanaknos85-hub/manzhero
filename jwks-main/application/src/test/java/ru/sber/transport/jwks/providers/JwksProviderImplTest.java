package ru.sber.transport.jwks.providers;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.jwks.web.providers.JwksProvider;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_jwks")
@DisplayName("Проверка JWKS провайдера")
class JwksProviderImplTest {

    private final JwksProvider jwksProvider = new JwksProviderImpl();

    @Test
    @DisplayName("Получение")
    void test_get() {
        ((JwksProviderImpl) jwksProvider).setPath("src/test/resources/jwt.key.pub");
        ((JwksProviderImpl) jwksProvider).setKid("jwt");
        ((JwksProviderImpl) jwksProvider).setAlgo("RS512");

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