package ru.sber.transport.trip.registries;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Точка входа в приложение
 */
@ConfigurationPropertiesScan
@Microservice
@OpenAPIDefinition(info = @Info(title = "Интеграция с порталом поставщика",
        description = "Операции по интеграционным взаимодействиям",
        version = "${spring.application.version}"),
        security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EntityScan(basePackages = {"ru.sber.transport.trip.registries"})
@ComponentScan(basePackages = {
        "ru.sber.transport.trip.registries"
})
@EnableScheduling
@Import(MapUtils.class)
public class RegistriesApplication {

    /**
     * Запуск приложения.
     *
     * @param args аргументы приложения.
     */
    public static void main(String... args) {
        SpringApplication.run(RegistriesApplication.class, args);
    }

}
