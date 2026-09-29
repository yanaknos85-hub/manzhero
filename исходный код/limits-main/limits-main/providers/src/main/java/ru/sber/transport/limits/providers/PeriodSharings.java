package ru.sber.transport.limits.providers;

import lombok.NonNull;
import ru.sber.transport.limits.model.Employee;
import ru.sber.transport.limits.model.PeriodSharing;
import ru.sber.transport.limits.model.Service;
import ru.sber.transport.limits.model.Type;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Распределения на период
 */
public interface PeriodSharings {

    /**
     * Получить распределение на период
     *
     * @param service  сервис
     *                 для которого осуществляется поиск
     * @param type     тип услуги распределения
     * @param date     дата
     * @param employee сотрудник
     * @param personal распределение только личное
     * @return распределение или отсутствует
     */
    Optional<PeriodSharing> get(@NonNull Service service, @NonNull Type type, @NonNull OffsetDateTime date, @NonNull Employee employee, boolean personal);

    /**
     * Обновить остаток на периоде
     *
     * @param periodSharing распределение на период
     * @param remains       остаток
     */
    void update(PeriodSharing periodSharing, BigDecimal remains);

    /**
     * Получить распределение на период по идентификатору
     *
     * @param id идентификатор
     * @return распределение
     */
    Optional<PeriodSharing> get(UUID id);
}
