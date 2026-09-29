package ru.sber.transport.fraud.monitoring.business;


import ru.sber.transport.fraud.monitoring.model.TripPurpose;

import java.util.UUID;

/**
 * Сервис работы с целями поездок
 */
public interface TripPurposesService {

    /**
     * Сохраняет цель поездки
     *
     * @param source цель поездки для сохранения
     * @return сохраненная должность
     */
    TripPurpose createOrUpdate(TripPurpose source);

    /**
     * Получает цель поездки по идентификатору
     *
     * @param id идентификатор цели поездки
     * @return должность
     */
    TripPurpose getExistedOrCreate(UUID id);
}
