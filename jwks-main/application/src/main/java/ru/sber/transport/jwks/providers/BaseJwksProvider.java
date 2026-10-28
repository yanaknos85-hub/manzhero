package ru.sber.transport.jwks.providers;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import ru.sber.transport.jwks.web.providers.JwksProvider;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Set;

/**
 * Базовые методы формирования JWKS.
 */
abstract class BaseJwksProvider implements JwksProvider {

    @Setter(AccessLevel.PROTECTED)
    @Value("${jwk.kid}")
    private String kid;

    @Setter(AccessLevel.PROTECTED)
    @Value("${jwk.algo}")
    private String algo;

    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    @Override
    public JWKSet get() {
        var data = Base64.getDecoder().decode(getData());
        var spec = new X509EncodedKeySpec(data);
        var kf = KeyFactory.getInstance("RSA");
        var key = (RSAPublicKey) kf.generatePublic(spec);
        var rsa = new RSAKey(key, KeyUse.SIGNATURE, Set.of(KeyOperation.VERIFY), Algorithm.parse(algo), kid,
            null, null, null, null, null, null, null, null);
        return new JWKSet(rsa);
    }

    /**
     * Получение данных, на основании которых необходимо собрать JWK.
     *
     * @return данные.
     */
    protected abstract String getData();

}
