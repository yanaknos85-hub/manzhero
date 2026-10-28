package ru.sber.transport.jwks.providers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sber.transport.jwks.web.providers.JwksProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.cloud.vault.enabled", havingValue = "false", matchIfMissing = true)
class JwksProviderImpl extends BaseJwksProvider implements JwksProvider {

    @Setter(AccessLevel.PACKAGE)
    @Value("${jwt.public.path:}")
    private String path;

    @SneakyThrows(IOException.class)
    @Override
    protected String getData() {
        return Files.readString(Path.of(path)).replace("\n", "");
    }
}
