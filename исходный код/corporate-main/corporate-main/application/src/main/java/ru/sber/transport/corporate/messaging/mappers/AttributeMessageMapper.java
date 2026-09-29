package ru.sber.transport.corporate.messaging.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.business.model.Attribute;

import java.util.Optional;

/**
 * Маппер данных атрибутов.
 */
@Mapper
public interface AttributeMessageMapper {

    /**
     * Конвертация сообщения в модель,
     *
     * @param source сообщение.
     * @return модель.
     */
    default String toMessage(Attribute source) {
        return Optional.ofNullable(source).map(Attribute::getName).orElse(null);
    }

}
