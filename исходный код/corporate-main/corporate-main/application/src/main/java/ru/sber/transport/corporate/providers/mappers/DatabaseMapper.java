package ru.sber.transport.corporate.providers.mappers;

import org.jooq.Record;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;

/**
 * Базовый маппер базы данных.
 *
 * @param <T> тип бизнес-объекта
 * @param <R> тип объекта базы данных
 */
public interface DatabaseMapper<T extends HasOrganizationStructure, R extends Record> {

    /**
     * Конвертировать сущность базы в бизнес.
     *
     * @param source исходная сущность.
     * @return целевая сущность.
     */
    T toBusiness(R source);

    /**
     * Конвертировать сущность бизнес в базу.
     *
     * @param target целевая сущность.
     * @param source исходная сущность.
     */
    void update(@MappingTarget R target, T source);

    /**
     * Конвертировать сущность бизнес в базу.
     *
     * @param position исходная сущность.
     * @return целевая сущность.
     */
    R toDatabase(T position);

}
