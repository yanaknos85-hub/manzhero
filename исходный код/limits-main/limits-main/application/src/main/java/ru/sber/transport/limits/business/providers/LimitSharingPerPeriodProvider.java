package ru.sber.transport.limits.business.providers;

import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Провайдер распределений средств на периоды
 */
public interface LimitSharingPerPeriodProvider {

    /**
     * Возвращает все распределения средств на период
     *
     * @param limitSharingId идентификатор распределения средств
     * @return коллекция распределений средств на период
     */
    List<LimitSharingPerPeriod> getAll(UUID limitSharingId);

    /**
     * Сохраняет распределения средств
     *
     * @param limitSharing распределения средств
     */
    void save(LimitSharingPerPeriod limitSharing);

    /**
     * Возвращает все распределения средств на период
     *
     * @param date дата
     * @return коллекция распределений средств на период
     */
    List<LimitSharingPerPeriod> get(LocalDate date);

    /**
     * Возвращает все распределения средств на период, которые ещё не были перемещены
     *
     * @param date дата
     * @return коллекция распределений средств на период
     */
    List<LimitSharingPerPeriod> getNotMoved(LocalDate date);

    /**
     * Возвращает распределение средств на период по идентификатору
     *
     * @param id идентификатор распределения
     * @return распределение средств на период
     */
    Optional<LimitSharingPerPeriod> get(UUID id);

    /**
     * Возвращает распределение средств на период по идентификатору и месяцу
     *
     * @param ids идентификаторы распределений средств
     * @param month месяц
     * @return распределение средств на периоды
     */
    List<LimitSharingPerPeriod> get(Set<UUID> ids, Month month);

    /**
     * Устанавливает флаг оповещения по распределению средств на период
     *
     * @param id идентификатор распределения средств на период
     */
    void setNotified(UUID id);
}
