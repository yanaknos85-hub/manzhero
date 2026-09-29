package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.Filter;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;
import ru.sber.transport.dto.Page;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;

import java.util.*;
import java.util.stream.Stream;

/**
 * Провайдер объектов базы данных.
 *
 * @param <R> тип объектов базы данных.
 */
public interface Provider<R extends HasOrganizationStructure, F extends Filter> {

    /**
     * Получить должность по ее идентификатору
     *
     * @param id идентификатор должности.
     * @return должность.
     */
    Optional<R> get(UUID id);

    /**
     * Получить все объекты.
     *
     * @return список объектов.
     */
    List<R> get();

    /**
     * Сохранить должность
     *
     * @param source должность.
     * @return сохраненная сущность.
     */
    R save(R source);

    /**
     * Получить все объекты.
     *
     * @param ids идентификаторы объектов.
     * @return список объектов.
     */
    Stream<R> streamAll(Collection<UUID> ids);

    /**
     * Сохранить все объекты.
     *
     * @param source объекты.
     * @return список сохраненных объектов.
     */
    Collection<R> saveAll(Collection<R> source);

    /**
     * Получить объект по идентификатору организации и идентификатору синхронизации.
     *
     * @param organization идентификатор организации.
     * @param syncId идентификатор синхронизации.
     * @return объект.
     */
    Optional<R> get(UUID organization, String syncId);

    /**
     * Сохранить объект синхронизации. Объект будет перезаписан в случае совпадения данных синхронизации
     *
     * @param source объект синхронизации.
     */
    void saveSync(R source);

    /**
     * Получить все объекты организации.
     *
     * @param filter фильтр поиска.
     * @param projection проекция полей.
     * @param page номер страницы.
     * @param size размер страницы.
     * @param sort название поля для сортировки.
     * @param direction направление сортировки.
     * @return страница с объектами.
     */
    Page<R> streamAll(F filter, Projection projection, Integer page, Integer size, String sort, Direction direction);
}
