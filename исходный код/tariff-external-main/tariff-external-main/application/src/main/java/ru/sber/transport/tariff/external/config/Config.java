package ru.sber.transport.tariff.external.config;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;
import ru.sber.transport.tariff.external.business.Links;
import ru.sber.transport.tariff.external.business.Tariffs;
import ru.sber.transport.tariff.external.business.impl.LinksImpl;
import ru.sber.transport.tariff.external.business.impl.TariffsImpl;
import ru.sber.transport.tariff.external.grpc.server.PriceDataServiceImpl;
import ru.sber.transport.tariff.external.providers.tariff.config.ExchangeProperties;
import ru.sber.transport.tariff.external.web.impl.PricesApiImpl;
import ru.sber.transport.web.api.PricesApi;

/**
 * Конфигурация сервиса получения тарифов
 */
@Slf4j
@NoArgsConstructor
@Configuration
public class Config {

    @Bean
    Tariffs businessTariffs(ru.sber.transport.tariff.external.providers.Tariffs tariffs) {
        log.info("Configuring business tariffs");
        return new TariffsImpl(tariffs);
    }

    @Bean
    ru.sber.transport.tariff.external.providers.Tariffs providersTariffs(ExchangeProperties properties, RestTemplate restTemplate) {
        log.info("Configuring providers tariffs");
        return new ru.sber.transport.tariff.external.providers.tariff.impl.TariffsImpl(properties, restTemplate);
    }

    @Bean
    PricesApi pricesApiDelegate(Tariffs tariffs) {
        log.info("Configuring prices api delegate");
        return new PricesApiImpl(tariffs);
    }

    @GrpcService
    PriceDataServiceGrpc.PriceDataServiceImplBase priceDataServiceGrpcClient(Tariffs tariffs, Links links) {
        log.info("Configuring prices grpc server");
        return new PriceDataServiceImpl(tariffs, links);
    }

    @Bean
    Links businessLinks(ru.sber.transport.tariff.external.providers.Links links) {
        log.info("Configuring business links");
        return new LinksImpl(links);
    }

    @Bean
    ru.sber.transport.tariff.external.providers.Links linksProvider() {
        log.info("Configuring links provider");
        return new ru.sber.transport.tariff.external.providers.links.LinksImpl();
    }

}
