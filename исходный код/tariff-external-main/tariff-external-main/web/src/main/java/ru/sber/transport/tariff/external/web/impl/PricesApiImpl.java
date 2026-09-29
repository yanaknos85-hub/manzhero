package ru.sber.transport.tariff.external.web.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.business.Tariffs;
import ru.sber.transport.web.api.PricesApi;
import ru.sber.transport.web.model.TariffData;
import ru.sber.transport.web.model.TariffItem;
import ru.sber.transport.web.model.TariffType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * Реализация внешнего API получения тарифов
 */
@RequiredArgsConstructor
public class PricesApiImpl implements PricesApi {

    private final Tariffs tariffs;

    @Override
    public ResponseEntity<TariffData> get(String coordinates, List<TariffType> type) {
        final var preparedCoordinates = prepareCoordinates(coordinates);
        final var preparedTypes = prepareType(Optional.ofNullable(type).orElseGet(() -> Arrays.stream(TariffType.values()).toList()));
        final var result = tariffs.get(preparedCoordinates, preparedTypes);
        if (!result.isEmpty()) {
            final var body = prepareResponse(result);
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    private TariffData prepareResponse(List<Tariff> tariffs) {
        final var tariffData = new TariffData();
        for (final var tariff : tariffs) {
            final var tariffItem = new TariffItem(BigDecimal.valueOf(tariff.distance()), TariffType.valueOf(tariff.type().name()), tariff.price());
            tariffItem.setTime(tariff.time().toString());
            tariffItem.setWaitTime(Optional.ofNullable(tariff.waitTime()).map(Duration::toString).orElse(null));
            tariffData.add(tariffItem);
        }
        return tariffData;
    }

    private Collection<Type> prepareType(List<TariffType> type) {
        return type.parallelStream().map(Enum::name).map(Type::valueOf).toList();
    }

    private List<Coordinates> prepareCoordinates(String coordinates) {
        final var coordinatesParts = coordinates.split(";");
        final var result = new ArrayList<Coordinates>();
        for (final var coordinatePart : coordinatesParts) {
            final var resulted = new WebCoordinates(coordinatePart);
            result.add(resulted);
        }
        return result;
    }

    private record WebCoordinates(double latitude, double longitude) implements Coordinates {

        public WebCoordinates(String coordinatePart) {
            this(Double.parseDouble(coordinatePart.split(",")[0]), Double.parseDouble(coordinatePart.split(",")[1]));
        }
    }
}
