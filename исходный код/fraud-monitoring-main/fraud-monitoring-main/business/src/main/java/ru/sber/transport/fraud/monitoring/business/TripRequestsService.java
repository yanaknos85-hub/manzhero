package ru.sber.transport.fraud.monitoring.business;


import ru.sber.transport.fraud.monitoring.model.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис работы с целями поездок
 */
public interface TripRequestsService {

    /**
     * Сохраняет заявку на поездку.
     *
     * @param source данные заявки
     * @return сохраненная заявка
     */
    TripRequest createOrUpdate(TripRequest source);

    /**
     * Получает заявку с нарушениями
     *
     * @param id идентификатор заявки на поездку
     * @return заявка на поездку
     */
    Optional<TripRequestDataWithMessages> get(UUID id);

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

}
