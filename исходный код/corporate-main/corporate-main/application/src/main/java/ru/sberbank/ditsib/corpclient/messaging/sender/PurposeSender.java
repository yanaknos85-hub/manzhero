package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;

/**
 * Отправка целей поездки.
 */
public interface PurposeSender {
    
    /**
     * Отправка данных.
     *
     * @param tripPurpose данные цели поездки.
     */
    void send(TripPurpose tripPurpose);
}
