package ru.sber.transport.tariff.external.providers.tariff.impl;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.providers.Tariffs;
import ru.sber.transport.tariff.external.providers.model.YandexClass;
import ru.sber.transport.tariff.external.providers.tariff.config.ExchangeProperties;
import ru.sber.transport.tariff.external.providers.tariff.exceptions.ServiceNotRespondException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация провайдера для получения тарифов.
 */
public class TariffsImpl implements Tariffs {

    private static final String URL = "/taxi_info";

    private final ExchangeProperties properties;

    private final RestTemplate restTemplate;

    public TariffsImpl(ExchangeProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Tariff> get(List<? extends Coordinates> coordinates, Collection<Type> tariffTypes) {
        final var request = createRequest(coordinates, tariffTypes);

        final var response = restTemplate.exchange(request, YandexTariff.class);
        return switch ((HttpStatus) response.getStatusCode()) {
            case NO_CONTENT -> List.of();
            case OK -> {
                final var body = response.getBody();
                assert body != null;
                yield body.options().stream()
                        .map(it -> createTariff(body, it))
                        .toList();
            }
            default -> throw new ServiceNotRespondException();
        };
    }

    private Tariff createTariff(YandexTariff tariff, Option option) {
        return new ru.sber.transport.tariff.external.providers.tariff.model.YandexTariff(
                option.className().getType(), BigDecimal.valueOf(option.price()), tariff.time(), option.waitTime(), (long) tariff.distance()
        );
    }

    @Override
    public String getName() {
        return "yandex";
    }

    private RequestEntity<Void> createRequest(List<? extends Coordinates> coordinates, Collection<Type> tariffTypes) {
        final var request = properties.getRequest();
        final var apiKey = request.apiKey();
        final var coordinatesBuilder = new StringBuilder();
        final var clientId = request.clientId();
        final var types = tariffTypes.stream().map(YandexClass::valueOf).map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(","));
        for (final var coordinate : coordinates) {
            if (!coordinatesBuilder.isEmpty()) {
                coordinatesBuilder.append("~");
            }
            coordinatesBuilder.append(coordinate.longitude()).append(",").append(coordinate.latitude());
        }
        final var url = "%s%s?clid=%s&rll=%s&class=%s&req=check&lang=ru".formatted(request.baseUrl(), URL, clientId, coordinatesBuilder, types);

        return RequestEntity.get(url)
                .header("YaTaxi-Api-Key", apiKey)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
