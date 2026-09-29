package ru.sber.transport.corporate.business.providers;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * Провайдер человекочитаемых идентификаторов
 */
public interface HumanReadableProvider<T> {

    /**
     * Получение следующего идентификатора.
     *
     * @return человекочитаемый идентификатор.
     */
    default String getNext(@NonNull UUID organizationId) {
        return getNext(organizationId, 1).getFirst();
    }

    /**
     * Получение следующего идентификатора.
     *
     * @return человекочитаемый идентификатор.
     */
    List<String> getNext(@NonNull UUID organizationId, int count);

    /**
     * Класс обслуживаемой сущности.
     *
     * @return класс обслуживаемой сущности.
     */
    Class<T> entityClass();

}
