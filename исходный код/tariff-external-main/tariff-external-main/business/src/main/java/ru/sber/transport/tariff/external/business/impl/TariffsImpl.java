package ru.sber.transport.tariff.external.business.impl;

import lombok.RequiredArgsConstructor;
import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.providers.Tariffs;

import java.util.Collection;
import java.util.List;

/**
 * Бизнес-логика работы с тарифами
 */
@RequiredArgsConstructor
public class TariffsImpl implements ru.sber.transport.tariff.external.business.Tariffs {

    private final Tariffs tariffs;

    @Override
    public List<Tariff> get(List<? extends Coordinates> coordinates, Collection<Type> tariffTypes) {
        return tariffs.get(coordinates, tariffTypes);
    }

}
