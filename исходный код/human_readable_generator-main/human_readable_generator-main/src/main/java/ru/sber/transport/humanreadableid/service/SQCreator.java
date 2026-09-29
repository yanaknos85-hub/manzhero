package ru.sber.transport.humanreadableid.service;

import ru.sber.transport.humanreadableid.model.BaseCompanySQ;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;

/**
 * Сервис создания сущности.
 *
 * @param <T> тип последовательности.
 */
public interface SQCreator<T extends BaseCompanySQ> {

    /**
     * Создание последовательности.
     *
     * @param prefix префикс.
     * @param organizationId корневой идентификатор.
     * @param count количество необходимых идентификаторов.
     *
     * @return последовательность.
     */
    T createCompanySQ(Prefix prefix, Long organizationId, int count);
}
