package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;

import java.util.UUID;

/**
 * Interface for sending cargo delivery time.
 */
public interface CargoDeliveryTimeSender {
    
    /**
     * Send delivery time data.
     *
     * @param cargoDeliveryTime to send.
     */
    void send(CargoDeliveryTime cargoDeliveryTime);
    
    /**
     * Send delete delivery time data.
     *
     * @param id id of delivery time to send.
     */
    void sendDeleted(UUID id);
}
