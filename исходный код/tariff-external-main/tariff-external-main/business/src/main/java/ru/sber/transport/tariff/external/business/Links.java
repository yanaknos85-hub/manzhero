package ru.sber.transport.tariff.external.business;

import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Type;

import java.net.URI;
import java.util.List;

/**
 * Бизнес-логика работы со ссылками
 */
public interface Links {

    /**
     * Получить ссылку по координатам
     * @param coordinates список координат
     * @param tariffType тип тарифа
     * @return ссылка
     */
    URI get(List<? extends Coordinates> coordinates, Type tariffType);

}
