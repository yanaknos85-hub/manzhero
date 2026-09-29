package ru.sber.transport.corporate.providers.attributes.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.business.model.Attribute;
import ru.sber.transport.database.corporate.tables.records.AttributeRecord;

/**
 * Маппер данных атрибутов.
 */
@Mapper
public interface AttributeDatabaseMapper {

    /**
     * Конвертировать модель базы в бизнес.
     *
     * @param source модель базы.
     * @return бизнес-модель.
     */
    Attribute toBusiness(AttributeRecord source);

}
