package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;

/**
 * Sending data about personal car.
 */
public interface PersonalCarSender {
    
    /**
     * Send personal car data.
     *
     * @param personalCar personal car data.
     * @param deleted is personal car deleted.
     */
    void send(PersonalCarDTO personalCar, boolean deleted);
}
