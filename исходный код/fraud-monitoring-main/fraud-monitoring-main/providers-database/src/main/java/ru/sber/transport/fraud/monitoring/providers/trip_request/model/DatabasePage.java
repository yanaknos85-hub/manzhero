package ru.sber.transport.fraud.monitoring.providers.trip_request.model;

import ru.sber.transport.fraud.monitoring.model.Page;
import ru.sber.transport.fraud.monitoring.model.PageData;
import ru.sber.transport.fraud.monitoring.model.SortData;
import ru.sber.transport.fraud.monitoring.model.TripRequestData;

import java.util.List;

/**
 * Класс для представления страницы заявок.
 *
 * @param content список заявок на странице
 * @param page    данные о странице
 * @param sort    данные о сортировке
 */
public record DatabasePage(List<TripRequestData> content, PageData page,
                           SortData sort) implements Page<TripRequestData> {

    /**
     * Конструктор класса DatabasePage.
     *
     * @param content список заявок на странице
     * @param page    данные о странице
     * @param size    количество элементов на странице
     * @param total   общее количество элементов
     * @param asc     признак сортировки в порядке возрастания
     * @param sort    поля сортировки
     */
    public DatabasePage(List<TripRequestData> content, int page, int size, int total, boolean asc, String sort) {
        this(content, new PageData() {

            @Override
            public int number() {
                return page;
            }

            @Override
            public int size() {
                return size;
            }

            @Override
            public boolean last() {
                return page == size;
            }

            @Override
            public boolean first() {
                return page == 0;
            }

            @Override
            public int total() {
                return total;
            }

            @Override
            public int count() {
                return size > 0 ? total / size : total;
            }
        }, new SortData() {

            @Override
            public String field() {
                return sort;
            }

            @Override
            public boolean asc() {
                return asc;
            }
        });
    }
}
