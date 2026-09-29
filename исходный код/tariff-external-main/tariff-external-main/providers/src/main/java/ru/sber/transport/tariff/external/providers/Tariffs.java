package ru.sber.transport.tariff.external.providers;

import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;

import java.util.Collection;
import java.util.List;

/**
 * Провайдер тарифов
 */
public interface Tariffs {

    /**
     * Получить все доступные тарифы
     *
     * @param coordinates - список координат, для которых необходимо получить тарифы
     * @param tariffTypes - список типов тарифов, которые необходимо получить
     * @return все доступные тарифы
     */
    List<Tariff> get(List<? extends Coordinates> coordinates, Collection<Type> tariffTypes);

    /**
     * Получить название провайдера
     *
     * @return название провайдера тарифов
     */
    String getName();

}
