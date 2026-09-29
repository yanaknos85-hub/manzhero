package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.TripPurpose;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер целей поездок.
 */
public interface TripPurposeProvider {

    /**
     * Получить цель поездки по идентификатору.
     *
     * @param id идентификатор.
     * @return цель поездки.
     */
    Optional<TripPurpose> get(UUID id);

}
