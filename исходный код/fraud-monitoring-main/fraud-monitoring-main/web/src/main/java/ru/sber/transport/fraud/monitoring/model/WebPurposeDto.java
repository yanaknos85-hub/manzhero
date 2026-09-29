package ru.sber.transport.fraud.monitoring.model;

import ru.sber.transport.web.model.PurposeDto;

/**
 * Объект ответа с данными о цели поездки
 */
public class WebPurposeDto extends PurposeDto {

    public WebPurposeDto(TripPurpose delegatee) {
        setId(delegatee.getId());
        setPurpose(delegatee.getLabel());
    }
}
