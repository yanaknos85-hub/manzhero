package ru.sber.transport.tariff_fleet;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@Microservice
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"ru.sber.transport.tariff_fleet", "ru.sber.transport.humanreadableid" })
@EntityScan(basePackages = { "ru.sber.transport.tariff_fleet", "ru.sber.transport.humanreadableid" })
@EnableJpaRepositories(
        basePackages = { "ru.sber.transport.tariff_fleet.database", "ru.sber.transport.humanreadableid" })
@OpenAPIDefinition(info = @Info(title = "Tariff Fleet",
        description = "Микросервис тарифов УКАП",
        version = "${spring.application.version}"),
        security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME),
        servers = {
                @Server(url = "http://api.fleet.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru/api/tariff-fleet/", description = "DEV fleet"),
                @Server(url = "http://api.ift.transport.apps.ift-terra000016-ids.ocp.delta.sbrf.ru/api/tariff-fleet/", description = "IFT"),
                @Server(url = "https://api.sowa-sigma-ift.sbertransport.ru/dev-fleet/api/tariff-fleet/", description = "DEV SOWA"),
                @Server(url = "https://api.sowa-sigma-ift.sbertransport.ru/api/tariff-fleet/", description = "IFT SOWA")
        })
@EnableTransactionManagement
@FileExchange
@EnableDiscoveryClient
@Import({ MapUtils.class })
public class Application {

    /**
     * Start a new application.
     *
     * @param args arguments of application.
     */
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

}
