package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.CargoType;

import java.util.UUID;

/**
 * Interface for sending cargo type.
 */
public interface CargoTypeSender {
    
    /**
     * Send type data.
     *
     * @param cargoType type to send.
     */
    void send(CargoType cargoType);
    
    /**
     * Send delete type data.
     *
     * @param id ID of type to send.
     */
    void sendDeleted(UUID id);
}
