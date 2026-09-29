package ru.sber.transport.tariff.external.business.impl;

import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff.external.business.Links;
import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Type;

import java.net.URI;
import java.util.List;

/**
 * Бизнес-кейс для работы со ссылками
 */
@RequiredArgsConstructor
public class LinksImpl implements Links {

    private final ru.sber.transport.tariff.external.providers.Links links;

    @Override
    public URI get(List<? extends Coordinates> coordinates, Type tariffType) {
        return links.get(coordinates, tariffType);
    }

}
