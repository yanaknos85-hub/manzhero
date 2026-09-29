package ru.sber.transport.tariff.external.providers.links;

import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.providers.Links;
import ru.sber.transport.tariff.external.providers.model.YandexClass;

import java.net.URI;
import java.util.List;

/**
 * Провайдер ссылок
 */
public class LinksImpl implements Links {

    private final static String BASE_URL = "https://3.redirect.appmetrica.yandex.com/route";

    @Override
    public URI get(List<? extends Coordinates> coordinates, Type tariffType) {
        final var uri = "%s?start-lat=%s&start-lon=%s&end-lat=%s&end-lon=%s&tariffClass=%s&appmetrica_tracking_id=1178268795219780156"
                .formatted(BASE_URL,
                        coordinates.get(0).latitude(),
                        coordinates.get(0).longitude(),
                        coordinates.get(1).latitude(),
                        coordinates.get(1).longitude(),
                        YandexClass.valueOf(tariffType).name().toLowerCase());
        return URI.create(uri);
    }

}
