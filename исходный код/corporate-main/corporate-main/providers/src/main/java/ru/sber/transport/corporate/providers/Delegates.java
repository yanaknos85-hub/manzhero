package ru.sber.transport.corporate.providers;

import ru.sber.transport.corporate.model.Delegate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер делегатов
 */
public interface Delegates {

    /**
     * Получить общее количество делегатов.
     *
     * @return общее количество делегатов.
     */
    long count();

    /**
     * Получает список делегатов.
     *
     * @param start позиция с которой нужно получить список.
     * @param limit ограничение по количеству делегатов.
     * @return список делегатов.
     */
    List<Delegate> get(int start, int limit);

    /**
     * Получить делегата по id.
     *
     * @param id идентификатор делегата.
     * @return делегат, если найден, иначе {@link Optional#empty()}.
     */
    Optional<Delegate> get(UUID id);
}
