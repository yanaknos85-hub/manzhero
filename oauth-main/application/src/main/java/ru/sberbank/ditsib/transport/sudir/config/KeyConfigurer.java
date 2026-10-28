package ru.sberbank.ditsib.transport.sudir.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Key configurer.
 */
@Slf4j
@Configuration
public class KeyConfigurer {

    @Bean
    KeyPair defaultKeyPair() throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }

    @Bean
    KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

    @Bean(value = "jwtPrivateKey")
    @Scope("singleton")
    Key jwtSecretKey(KeyPair defaultKeyPair, KeyFactory factory, @Value("${jwt.key.private.path:classpath:key/jwt.key}") String secretKeyPath) throws InvalidKeySpecException {
        try {
            var pem = Files.readString(Path.of(secretKeyPath))
                .replaceFirst("-----BEGIN PRIVATE KEY-----", "")
                .replaceFirst("-----END PRIVATE KEY-----", "");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
        } catch (IOException e) {
            return defaultKeyPair.getPrivate();
        }
    }
    
}
