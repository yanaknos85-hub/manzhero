package ru.sber.transport.corporate_sync.mappers;

import org.apache.avro.specific.SpecificRecord;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;

/**
 * Маппер сообщений
 *
 * @param <T> тип данных в системе
 * @param <M> тип сообщения
 */
public interface MessageMapper<T extends HasOrganizationStructure, M extends SpecificRecord> {

    /**
     * Обновить объект системы
     *
     * @param target объект системы
     * @param source сообщение
     */
    void update(@MappingTarget T target, M source);

    /**
     * Конвертация сообщения в модель
     *
     * @param data сообщение
     * @return модель
     */
    T toBusiness(M data);
}
