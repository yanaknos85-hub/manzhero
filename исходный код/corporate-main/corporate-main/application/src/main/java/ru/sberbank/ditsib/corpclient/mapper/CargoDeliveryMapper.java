package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;
import ru.sberbank.ditsib.transport.messaging.messages.CargoDeliveryTimeMessage;

/**
 * Mapper of cargo delivery data.
 */
@Mapper
public interface CargoDeliveryMapper {

    /**
     * Convert entity to message.
     *
     * @param entity source entity.
     * @return message.
     */
    CargoDeliveryTimeMessage toMessage(CargoDeliveryTime entity);

}
