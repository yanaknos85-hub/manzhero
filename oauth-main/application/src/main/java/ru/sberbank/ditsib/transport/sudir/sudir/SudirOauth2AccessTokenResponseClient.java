package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Similar to {@link org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient} (final)
 * Customized rest template &amp; converter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SudirOauth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";
    
    private final Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter;

    private final ApplicationContext applicationContext;

    @Autowired
    public SudirOauth2AccessTokenResponseClient(ApplicationContext applicationContext,
                                                Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter) {
        this.applicationContext = applicationContext;
        this.requestEntityConverter = requestEntityConverter;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        log.debug("DEBUGSUDIR point 1");
        Assert.notNull(authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null");
        var request = this.requestEntityConverter.convert(authorizationCodeGrantRequest);

        var clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
        log.debug("DEBUGSUDIR: " + clientRegistration.toString());
        
        var response = getResponse(Objects.requireNonNull(request));
        log.debug("DEBUGSUDIR point 2");
        var tokenResponse = response.getBody();
        if (tokenResponse != null && CollectionUtils.isEmpty(tokenResponse.getAccessToken().getScopes())) {
            tokenResponse = OAuth2AccessTokenResponse.withResponse(tokenResponse)
                .scopes(authorizationCodeGrantRequest.getClientRegistration().getScopes())
                .build();
        }
        return tokenResponse;
    }

    private ResponseEntity<OAuth2AccessTokenResponse> getResponse(@NonNull RequestEntity<?> request) {
        try {
            var restTemplate = applicationContext.getBean(RestTemplate.class);
            restTemplate.setMessageConverters(
                List.of(new FormHttpMessageConverter(),
                    new OAuth2AccessTokenResponseHttpMessageConverter(),
                    new StringHttpMessageConverter(StandardCharsets.UTF_8)));
            restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
            return restTemplate.exchange(request, OAuth2AccessTokenResponse.class);
        } catch (RestClientException ex) {
            var oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response"
                    + ex.getMessage(),
                request.getUrl().toString());
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }
    }
}
