package ru.sber.transport.jwks.providers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import ru.sber.transport.jwks.web.providers.JwksProvider;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.cloud.vault.enabled", havingValue = "true")
class JwksVaultProviderImpl extends BaseJwksProvider implements JwksProvider {

    private final VaultTemplate vaultTemplate;

    @Setter(AccessLevel.PACKAGE)
    @Value("${spring.cloud.vault.jwt.path}")
    private String path;

    @Setter(AccessLevel.PACKAGE)
    @Value("${spring.cloud.vault.jwt.name}")
    private String propertyName;

    @Override
    protected String getData() {
        var response = vaultTemplate.read(path);
        var data = response.getData();
        if (data == null) {
            return null;
        }
        var rawKey = data.get(propertyName);
        if (rawKey != null) {
            return String.valueOf(rawKey);
        }
        return null;
    }

}
