package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер данных о заявках на поездки.
 */
public interface TripRequestsDatabaseProvider {

    /**
     * Сохраняет заявку на поездку.
     *
     * @param source данные заявки
     * @return сохраненная заявка
     */
    TripRequest createOrUpdate(TripRequest source);

    /**
     * Проверяет, существует ли заявка с указанным идентификатором.
     *
     * @param id идентификатор заявки
     * @return true, если заявка существует, иначе false
     */
    boolean exists(UUID id);

    /**
     * Создает заявку только с ID, если она не существует.
     *
     * @param id идентификатор заявки
     * @return true, если заявка была создана, false если уже существовала
     */
    boolean createIfNotExists(UUID id);

    /**
     * Получает заявку с нарушениями
     *
     * @param id идентификатор заявки на поездку
     * @return заявка на поездку
     */
    Optional<TripRequestData> get(UUID id);

    /**
     * Получает отфильтрованный список заявок на поездку
     *
     * @param filter фильтр
     * @param page   страница
     * @param size   количество заявок на странице
     * @param sort   свойство по которому сортируется список заявок на поездку
     * @param asc    сортировка по возрастанию
     * @return список заявок на поездку
     */
    Page<TripRequestData> get(RequestFilter filter, int page, int size, String sort, boolean asc);

    /**
     * Получает заявку на поездку с нарушениями и сообщениями по ним.
     *
     * @param id идентификатор заявки на поездку
     * @return заявка на поездку с нарушениями, или {@link Optional#empty()}, если не найдена
     */
    Optional<TripRequestDataWithMessages> getWithMessages(UUID id);
}
