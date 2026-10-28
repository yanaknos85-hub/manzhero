package ru.sberbank.ditsib.transport.sudir.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    CorsFilter corsFilter(@Value("${cors.allowed.origin:*}") String origin,
                          @Value("${cors.allowed.header:*}") String header) {
        var source = new UrlBasedCorsConfigurationSource();
        var config = new CorsConfiguration();
        config.addAllowedOrigin(origin);
        config.addAllowedHeader(header);
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
