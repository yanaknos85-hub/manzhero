package ru.sber.transport.authorization.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.security.NoAuthorized;
import ru.sber.transport.authorization.service.BlackListService;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Basic auth security adapter.
 */
@Configuration(proxyBeanMethods = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Primary
@Slf4j
@RequiredArgsConstructor
public class SecurityConfigurerAdapter {

    private final List<NoAuthorized> noAuthorizeds;

    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            HandlerMappingIntrospector introspector,
            List<AuthorizationManager<RequestAuthorizationContext>> accessManagers,
            Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oauthConfiguration,
            Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer,
            Customizer<CsrfConfigurer<HttpSecurity>> csrfCustomizer,
            Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementCustomizer,
            Customizer<ExceptionHandlingConfigurer<HttpSecurity>> exceptionHandlingCustomizer,
            Customizer<FormLoginConfigurer<HttpSecurity>> formLoginCustomizer,
            Customizer<LogoutConfigurer<HttpSecurity>> logoutCustomizer) throws Exception {
        log.info("Create filter chain for authorization");
        var noAuthorize = addPermissions(introspector);
        if (noAuthorize.length > 0) {
            http.securityMatchers(it -> it.requestMatchers(noAuthorize))
                    .authorizeHttpRequests(it -> it.requestMatchers(noAuthorize).permitAll())
                    .sessionManagement(sessionManagementCustomizer)
                    .exceptionHandling(exceptionHandlingCustomizer)
                    .formLogin(formLoginCustomizer)
                    .logout(logoutCustomizer)
                    .cors(corsCustomizer).csrf(csrfCustomizer);
        }
        return http.securityMatchers(AbstractRequestMatcherRegistry::anyRequest)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().access((authentication, object) -> accessManagers.stream()
                        .map(manager -> manager.authorize(authentication, object))
                        .filter(Objects::nonNull)
                        .reduce((l, r) -> new AuthorizationDecision(l.isGranted() && r.isGranted()))
                        .map(AuthorizationDecision.class::cast)
                        .orElseGet(() -> AuthenticatedAuthorizationManager.fullyAuthenticated().check(authentication, object))))
                .sessionManagement(sessionManagementCustomizer)
                .oauth2ResourceServer(oauthConfiguration)
                .cors(corsCustomizer).csrf(csrfCustomizer)
                .exceptionHandling(exceptionHandlingCustomizer)
                .formLogin(formLoginCustomizer)
                .logout(logoutCustomizer)
                .build();
    }

    @Bean
    public Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementCustomizer() {
        log.info("Create session management configurer");
        return it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public Customizer<ExceptionHandlingConfigurer<HttpSecurity>> exceptionHandlingCustomizer() {
        log.info("Create exception handling configurer");
        return it -> it.authenticationEntryPoint((request, response, authException) -> sendUnauthorized(request, response));
    }

    @Bean
    public Customizer<CsrfConfigurer<HttpSecurity>> csrfCustomizer() {
        log.info("Create csrf configurer");
        return AbstractHttpConfigurer::disable;
    }

    @Bean
    public Customizer<FormLoginConfigurer<HttpSecurity>> formLoginCustomizer() {
        log.info("Create form login configurer");
        return AbstractHttpConfigurer::disable;
    }

    @Bean
    public Customizer<LogoutConfigurer<HttpSecurity>> logoutCustomizer() {
        log.info("Create logout configurer");
        return AbstractHttpConfigurer::disable;
    }

    @Bean
    public Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer(CorsConfigurationSource corsConfigurationSource) {
        log.info("Create cors configurer");
        return httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource);
    }

    @Primary
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed.origin:*}") String origin,
                                                           @Value("${cors.allowed.header:*}") String header) {
        log.info("Create cors configuration source");
        var config = new CorsConfiguration();
        config.addAllowedOrigin(origin);
        config.addAllowedHeader(header);
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");

        var corsConfigurations = Map.of("/**", config);

        var source = new UrlBasedCorsConfigurationSource();
        source.setCorsConfigurations(corsConfigurations);
        return source;
    }

    @Bean
    public Customizer<HttpBasicConfigurer<HttpSecurity>> httpBasicCustomizer() {
        log.info("Create http basic configurer");
        return httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer.realmName("Basic");
    }

    @Bean
    public AuthenticationProvider basicAuthenticationProvider(Map<String, UsernamePasswordAuthenticationToken> tokens) {
        log.info("Create basic authentication provider");
        return new AuthenticationProvider() {

            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return tokens.getOrDefault((String) authentication.getPrincipal(), (UsernamePasswordAuthenticationToken) authentication);
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    public Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oauthConfiguration(Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer> jwtConfigurer) {
        log.info("Create oauth2 resource server configurer");
        return httpSecurityOAuth2ResourceServerConfigurer -> httpSecurityOAuth2ResourceServerConfigurer.jwt(jwtConfigurer);
    }

    @Bean
    public Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer> jwtConfigurer(ObjectProvider<BlackListService> blackListService) {
        log.info("Create jwt configurer");
        return jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(new JwtConverter(blackListService));
    }

    /**
     * Send unauthorized response.
     *
     * @param req requested data.
     * @param res response to send data.
     */
    @SneakyThrows(IOException.class)
    private void sendUnauthorized(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setHeader("WWW-Authenticate", "Bearer realm=Access");
        res.setHeader("Content-Type", "application/json");

        var referer = Optional.ofNullable(req.getHeader("referer")).map(URI::create).map(URI::getPath).orElse("localhost");
        var content =
                """
                        {
                            "timestamp": "%s",
                            "status": "%s",
                            "message": "Your token is corrupt or invalid",
                            "error": "Unauthorized",
                            "path": "%s"
                        }
                        """
                        .formatted(LocalDateTime.now(ZoneOffset.UTC), res.getStatus(), referer);


        var writer = res.getOutputStream();
        writer.println(content);
        writer.flush();
    }

    /**
     * Add permissions to methods.
     */
    private RequestMatcher[] addPermissions(HandlerMappingIntrospector introspector) {
        var additionalPermissions = noAuthorizeds.parallelStream().flatMap(a -> a.getUrls().parallelStream()).collect(Collectors.toSet());
        if (log.isDebugEnabled()) {
            log.debug("Urls without authorization: %s%s".formatted(System.lineSeparator(), additionalPermissions.parallelStream().map(e -> "%s %s".formatted(e.getKey(), e.getValue())).collect(Collectors.joining(System.lineSeparator()))));
        }
        return additionalPermissions.parallelStream().map(it -> new MvcRequestMatcher.Builder(introspector).pattern(it.getKey(), it.getValue())).toArray(RequestMatcher[]::new);
    }

    private static class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

        private final ObjectProvider<BlackListService> blackListService;

        public JwtConverter(ObjectProvider<BlackListService> blackListService) {
            this.blackListService = blackListService;
            if (blackListService.getIfAvailable() == null) {
                log.warn("Expired tokens provider is not initialized");
            }
        }

        @Override
        public AbstractAuthenticationToken convert(@NonNull Jwt source) {
            final var service = blackListService.getIfAvailable();
            if (service != null && service.check(source.getTokenValue())) {
                throw new UnauthorizedException("Token is blocked");
            }
            return new JwtAuthenticationToken(source, source.getClaimAsStringList("roles").parallelStream().map(SimpleGrantedAuthority::new).toList());
        }
    }

}
