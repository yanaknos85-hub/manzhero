package ru.sber.transport.tariff.external.providers;

import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Type;

import java.net.URI;
import java.util.List;

/**
 * Провайдер ссылок
 */
public interface Links {

    /**
     * Получить ссылку заявок
     *
     * @param coordinates список координат
     * @param tariffType  тип тарифа
     * @return ссылка
     */
    URI get(List<? extends Coordinates> coordinates, Type tariffType);

}
