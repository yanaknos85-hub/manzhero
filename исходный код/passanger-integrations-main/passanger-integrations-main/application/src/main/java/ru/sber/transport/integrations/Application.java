package ru.sber.transport.integrations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.integrations.config.JsonApiProperties;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@ConfigurationPropertiesScan
@Microservice
@EnableTransactionManagement
@EnableFeignClients(basePackages = {
        "ru.sber.transport.integrations.feign",
        "ru.sber.transport.integrations"
})
@EnableConfigurationProperties(JsonApiProperties.class)
@OpenAPIDefinition(info = @Info(title = "Интеграции",
                                description = "Операции по интеграционным взаимодействиям",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EntityScan(basePackages = { "ru.sber.transport.integrations" })
@ComponentScan(basePackages = {
        "ru.sber.transport.integrations",
        "ru.sber.transport.integrations.service.impl",
        "ru.sberbank.ditsib.transport.logging",
        "ru.sberbank.ditsib.transport"
})
@EnableScheduling
public class Application {
    
    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }
    
    
}
