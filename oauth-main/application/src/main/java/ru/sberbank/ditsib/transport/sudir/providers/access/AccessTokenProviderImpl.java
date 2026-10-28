package ru.sberbank.ditsib.transport.sudir.providers.access;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.providers.AccessTokenProvider;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.BlackListSender;
import ru.sberbank.ditsib.transport.sudir.providers.access.config.JwtProperties;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.model.Scope;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Реализация провайдера токенов доступа.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class AccessTokenProviderImpl implements AccessTokenProvider {

    /**
     * Отправитель данных о токенах в черный список.
     */
    private final BlackListSender blackListSender;

    private final RoleRepository roleRepository;

    private final AccountRepository accountRepository;

    private final Tracer tracer;

    /**
     * Секретный ключ JWT.
     */
    @Qualifier("jwtPrivateKey")
    private final Key jwtKey;

    /**
     * Настройки JWT.
     */
    private final JwtProperties jwtProperties;

    private final KeyFactory keyFactory;

    @SuppressWarnings("java:S3958")
    @Override
    public String generate(Account accountDto) throws InvalidKeySpecException {
        log.debug("Generating access token for {} started at {}", accountDto.getPersonnelNumber(), LocalDateTime.now(ZoneOffset.UTC));

        var expiration = jwtProperties.getExpire();

        var issuedAt = OffsetDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));

        var expireAt = issuedAt
            .plusSeconds(expiration.getSeconds())
            .plusMinutes(expiration.getMinutes())
            .plusHours(expiration.getHours())
            .plusDays(expiration.getDays())
            .plusMonths(expiration.getMonths())
            .plusYears(expiration.getYears());

        var account = accountRepository.getByPersonnelNumber(accountDto.getPersonnelNumber());
        var roles = roleRepository.findOfAccount(account);

        var jwsHeaders = JwsHeader
            .with(SignatureAlgorithm.RS512)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .type("JWT")
            .build();
        var jwsClaims = JwtClaimsSet.builder()
            .claim(ROLES_CLAIM_NAME, roles.stream().map(RoleRecord::getCode).toList())
            .claim(SCOPE_CLAIM_NAME, accountDto.getScope() == null ? Scope.EMPLOYEE : accountDto.getScope())
            .claim(RANDOM_CLAIM_NAME, UUID.randomUUID())
            .claim(REQUEST_ID_CLAIM_NAME, Optional.ofNullable(tracer.currentSpan()).map(Span::context).map(TraceContext::traceId)
                .orElse(String.valueOf(UUID.randomUUID())))
            .expiresAt(expireAt.toInstant())
            .issuer(jwtProperties.getIssuer())
            .issuedAt(issuedAt.toInstant())
            .notBefore(issuedAt.toInstant())
            .subject(accountDto.getPersonnelNumber())
            .id(accountDto.getId().toString())
                .claim(DATAMASTER_CLAIM_NAME, roles.stream().anyMatch(RoleRecord::getDataMaster));
        var parameters = JwtEncoderParameters.from(jwsHeaders, jwsClaims.build());

        var jwk = createJwk();
        var jwksList = new JWKSet(jwk);
        var jwks = new ImmutableJWKSet<>(jwksList);
        var encoder = new NimbusJwtEncoder(jwks);
        var jwt = encoder.encode(parameters);

        return jwt.getTokenValue();
    }

    private JWK createJwk() throws InvalidKeySpecException {
        var privateKey = (PrivateKey) jwtKey;
        var publicKey = createPublic(privateKey);
        var use = KeyUse.SIGNATURE;
        var ops = Set.of(KeyOperation.SIGN);
        var alg = Algorithm.parse(SignatureAlgorithm.RS512.name());
        var kid = getKid();
        return new RSAKey(
            publicKey,
            privateKey,
            use,
            ops,
            alg,
            kid,
            null, null, null, null, null, null, null, null);
    }

    private @NotNull String getKid() {
        return "jwt";
    }

    private RSAPublicKey createPublic(PrivateKey privateKey) throws InvalidKeySpecException {
        RSAPrivateCrtKey privk = (RSAPrivateCrtKey)privateKey;
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
        return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    @Override
    public void toBlackList(String token) {
        blackListSender.send(token);
    }
}
