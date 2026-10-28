package ru.sberbank.ditsib.transport.sudir.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.sberbank.ditsib.transport.sudir.sudir.*;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.DatabaseOAuth2AuthorizationRequestProvider;

import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SudirSecurityConfigurerAdapter {

    private static final String[] EXCLUDES = new String[]{
            "/login/oauth2/code/sudir",
            "/favicon.ico",
            "/login",
            "/error",
            "/failed",
            "/success",
            "/logout",
            "/actuator/**",
            "/roles/**",
            "/unauthorized"
    };

    private final SudirOidcUserService sudirOidcUserService;

    private final SudirOauth2AccessTokenResponseClient sudirOauth2AccessTokenResponseClient;

    private final SudirSuccessHandler sudirSuccessHandler;

    private final SudirFailureHandler sudirFailureHandler;

    private final SudirAuthenticationEntryPoint authenticationEntryPoint;

    private final DefaultRedirectStrategy defaultRedirectStrategy;

    private final DatabaseOAuth2AuthorizationRequestProvider databaseOAuth2AuthorizationRequestProvider;

    @Bean
    public SecurityFilterChain security(HttpSecurity httpSecurity,
                                        Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer) throws Exception {
        var builder = httpSecurity
                .authorizeHttpRequests(ex -> ex.requestMatchers(EXCLUDES).permitAll().anyRequest().authenticated())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(customize -> customize.failureHandler(sudirFailureHandler)
                        .userInfoEndpoint(custom -> custom.oidcUserService(sudirOidcUserService))
                        .tokenEndpoint(custom -> custom.accessTokenResponseClient(sudirOauth2AccessTokenResponseClient))
                        .authorizationEndpoint(custom -> custom.authorizationRedirectStrategy(defaultRedirectStrategy)
                                .authorizationRequestRepository(databaseOAuth2AuthorizationRequestProvider))
                        .successHandler(sudirSuccessHandler)
                )
                .exceptionHandling(custom -> custom.authenticationEntryPoint(authenticationEntryPoint))
                .cors(corsCustomizer)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return builder.build();
    }

    @Bean
    Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer(CorsConfigurationSource corsConfigurationSource) {
        return httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed.origin:*}") String origin,
                                                    @Value("${cors.allowed.header:*}") String header) {
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
}
