package ru.sber.transport.limits.providers;

import ru.sber.transport.limits.model.PeriodSharing;
import ru.sber.transport.limits.model.Reserve;
import ru.sber.transport.limits.model.ReserveStatus;
import ru.sber.transport.limits.model.Spending;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер данных резервов
 */
public interface Spendings {

    /**
     * Получить резерв по идентификатору операции
     *
     * @param id идентификатор
     * @return резерв или отсутствует
     */
    Optional<Spending> get(UUID id);

    /**
     * Обновить статус резерва
     *
     * @param reserve резерв
     * @param status  новый статус
     */
    void update(Spending reserve, ReserveStatus status);

    /**
     * Создать резерв на операцию
     *
     * @param sharing распределение на период
     * @param data    данные о резерве
     */
    void create(PeriodSharing sharing, Reserve data);

}
