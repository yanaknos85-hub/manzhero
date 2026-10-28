package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.converter.ClaimConversionService;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.ditsib.transport.sudir.config.properties.SudirProperties;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Similar to {@link org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory} (final)
 * Customized {@link JwtDecoder} building process &amp; jwt validation
 */
@Component
@Primary
@RequiredArgsConstructor
public class SudirOpenIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {
    private static final String MISSING_SIGNATURE_VERIFIER_ERROR_CODE = "missing_signature_verifier";
    private static final ClaimTypeConverter DEFAULT_CLAIM_TYPE_CONVERTER = new ClaimTypeConverter(
        createDefaultClaimTypeConverters());
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private static final Function<ClientRegistration, JwsAlgorithm> jwsAlgorithmResolver = cr -> SignatureAlgorithm.RS256;
    private static final Function<ClientRegistration, Converter<Map<String, Object>, Map<String, Object>>> claimTypeConverterFactory = cr -> DEFAULT_CLAIM_TYPE_CONVERTER;
    private final SudirProperties properties;
    private Function<ClientRegistration, OAuth2TokenValidator<Jwt>> jwtValidatorFactory;

    /**
     * Returns the default {@link Converter}'s used for type conversion of claim values
     * for an {@link OidcIdToken}.
     *
     * @return a {@link Map} of {@link Converter}'s keyed by {@link IdTokenClaimNames
     * claim name}
     */
    private static Map<String, Converter<Object, ?>> createDefaultClaimTypeConverters() {
        Converter<Object, ?> booleanConverter = getConverter(TypeDescriptor.valueOf(Boolean.class));
        Converter<Object, ?> instantConverter = getConverter(TypeDescriptor.valueOf(Instant.class));
        Converter<Object, ?> urlConverter = getConverter(TypeDescriptor.valueOf(URL.class));
        Converter<Object, ?> stringConverter = getConverter(TypeDescriptor.valueOf(String.class));
        Converter<Object, ?> collectionStringConverter = getConverter(
            TypeDescriptor.collection(Collection.class, TypeDescriptor.valueOf(String.class)));
        Map<String, Converter<Object, ?>> converters = new HashMap<>();
        converters.put(IdTokenClaimNames.ISS, urlConverter);
        converters.put(IdTokenClaimNames.AUD, collectionStringConverter);
        converters.put(IdTokenClaimNames.NONCE, stringConverter);
        converters.put(IdTokenClaimNames.EXP, instantConverter);
        converters.put(IdTokenClaimNames.IAT, instantConverter);
        converters.put(IdTokenClaimNames.AUTH_TIME, instantConverter);
        converters.put(IdTokenClaimNames.AMR, collectionStringConverter);
        converters.put(StandardClaimNames.EMAIL_VERIFIED, booleanConverter);
        converters.put(StandardClaimNames.PHONE_NUMBER_VERIFIED, booleanConverter);
        converters.put(StandardClaimNames.UPDATED_AT, instantConverter);
        return converters;
    }

    private static Converter<Object, ?> getConverter(TypeDescriptor targetDescriptor) {
        var sourceDescriptor = TypeDescriptor.valueOf(Object.class);
        return source -> ClaimConversionService.getSharedInstance().convert(source, sourceDescriptor,
            targetDescriptor);
    }

    @PostConstruct
    public void initJwtValidatorFactory() {
        jwtValidatorFactory = clientRegistration -> {
            var oidcIdTokenValidator = new OidcIdTokenValidator(clientRegistration);
            var clockSkew = Duration.ofSeconds(properties.getClockSkewSeconds());
            oidcIdTokenValidator.setClockSkew(clockSkew);
            return new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(clockSkew),
                oidcIdTokenValidator);
        };
    }

    @Override
    public JwtDecoder createDecoder(ClientRegistration clientRegistration) {
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        return this.jwtDecoders.computeIfAbsent(clientRegistration.getRegistrationId(), key -> {
            NimbusJwtDecoder jwtDecoder = buildDecoder(clientRegistration);
            jwtDecoder.setJwtValidator(this.jwtValidatorFactory.apply(clientRegistration));
            Converter<Map<String, Object>, Map<String, Object>> claimTypeConverter = claimTypeConverterFactory
                .apply(clientRegistration);
            if (claimTypeConverter != null) {
                jwtDecoder.setClaimSetConverter(claimTypeConverter);
            }
            return jwtDecoder;
        });
    }

    private NimbusJwtDecoder buildDecoder(ClientRegistration clientRegistration) {
        var jwsAlgorithm = jwsAlgorithmResolver.apply(clientRegistration);
        String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
        if (!StringUtils.hasText(jwkSetUri)) {
            var oauth2Error = new OAuth2Error(MISSING_SIGNATURE_VERIFIER_ERROR_CODE,
                "Failed to find a Signature Verifier for Client Registration: '"
                    + clientRegistration.getRegistrationId()
                    + "'. Check to ensure you have configured the JwkSet URI.",
                null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        restTemplate.setMessageConverters(Collections.singletonList(new StringHttpMessageConverter()));
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).restOperations(restTemplate)
            .jwsAlgorithm((SignatureAlgorithm) jwsAlgorithm).build();
    }
}
