package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;

import java.util.List;

/**
 * Sending data about position.
 */
public interface PositionSender extends Sender<Position> {
    
    /**
     * Send position data.
     *
     * @param position position data.
     * @param deleted is position deleted.
     */
    void send(PositionDTO position, boolean deleted);
    
    /**
     * Отправить данные должности.
     *
     * @param position должность.
     */
    default void send(Position position) {
        send(List.of(position));
    }
}
