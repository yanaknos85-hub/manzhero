package ru.sberbank.ditsib.transport.limits;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Лимиты",
                                description = "Операции по работе с лимитами",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@ComponentScan(basePackages = { "ru.sber", "ru.sberbank.ditsib.transport.limits", "ru.sber.transport.humanreadableid" })
@EnableAsync
@EnableDiscoveryClient
@EnableTransactionManagement
public class LimitsApplication {
    
    /**
     * Start a new application.
     *
     * @param args arguments of application.
     */
    public static void main(String... args) {
        SpringApplication.run(LimitsApplication.class, args);
    }

}