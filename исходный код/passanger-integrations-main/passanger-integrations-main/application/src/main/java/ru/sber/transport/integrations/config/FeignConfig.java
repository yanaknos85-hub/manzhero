package ru.sber.transport.integrations.config;

import feign.Client;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Конфиг feign клиента.
 */
@Configuration
public class FeignConfig {


    /**
     * Конфигурация Feign Client отключение проверки валидности ssl совы.
     *
     * @return Http Client.
     */
    @Bean
    public Client feignClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final var factory = SSLContextBuilder.create()
                         .loadTrustMaterial((x509Certificates, s) -> true).build().getSocketFactory();
        return new Client.Default(factory, new NoopHostnameVerifier());
    }
}
