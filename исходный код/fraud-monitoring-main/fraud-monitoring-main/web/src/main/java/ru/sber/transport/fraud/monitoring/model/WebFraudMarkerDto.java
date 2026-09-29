package ru.sber.transport.fraud.monitoring.model;

import ru.sber.transport.web.model.FraudMarkerDto;

/**
 * Объект ответа с данными о нарушении (фроде)
 */
public class WebFraudMarkerDto extends FraudMarkerDto {

    public WebFraudMarkerDto(Fraud delegatee) {
        setId(delegatee.getId());
        setComment(delegatee.getComment());
    }
}
