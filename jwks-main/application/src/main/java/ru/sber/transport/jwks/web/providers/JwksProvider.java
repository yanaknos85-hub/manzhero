package ru.sber.transport.jwks.web.providers;

import com.nimbusds.jose.jwk.JWKSet;

/**
 * Провайдер хранилища ключей.
 */
public interface JwksProvider {

    /**
     * Получить коллекцию ключей.
     *
     * @return коллекция ключей.
     */
    JWKSet get();

}
