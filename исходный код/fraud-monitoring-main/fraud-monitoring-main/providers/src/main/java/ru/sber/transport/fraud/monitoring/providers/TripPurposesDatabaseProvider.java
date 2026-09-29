package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.TripPurpose;

import java.util.UUID;

/**
 * Провайдер данных о целях поездки.
 */
public interface TripPurposesDatabaseProvider {

    /**
     * Сохраняет данные о цели поездки
     *
     * @param source данные о цели поездки
     * @return сохраненные данные о цели поездки
     */
    TripPurpose createOrUpdate(TripPurpose source);

    /**
     * Получает данные о цели поездки
     *
     * @param id идентификатор цели поездки
     * @return данные о цели поездки
     */
    TripPurpose get(UUID id);

}
