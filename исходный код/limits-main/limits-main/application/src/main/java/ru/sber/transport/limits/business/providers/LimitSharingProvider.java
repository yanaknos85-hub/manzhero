package ru.sber.transport.limits.business.providers;

import ru.sber.transport.limits.business.model.LimitSharing;

import java.util.List;
import java.util.UUID;

/**
 * Провайдер распределений средств
 */
public interface LimitSharingProvider {

    /**
     * Возвращает все распределения средств
     *
     * @param limitId идентификатор лимита
     * @return коллекция распределений средств
     */
    List<LimitSharing> getAll(UUID limitId);

    /**
     * Сохраняет распределения средств
     *
     * @param source распределения средств
     */
    void save(LimitSharing source);
}
