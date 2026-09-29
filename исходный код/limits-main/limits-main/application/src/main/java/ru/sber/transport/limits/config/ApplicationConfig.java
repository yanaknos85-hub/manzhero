package ru.sber.transport.limits.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Slf4j
@Configuration
public class ApplicationConfig {

    @PostConstruct
    void init() {
        log.info("Starting SpEL components");
    }

    @Bean
    public SpelParserConfiguration spelParserConfiguration() {
        return new SpelParserConfiguration(false, true);
    }

    @Bean
    public SpelExpressionParser spelExpressionParser(SpelParserConfiguration configuration) {
        return new SpelExpressionParser(configuration);
    }
}
