package ru.sber.transport.humanreadableid.service;

import ru.sber.transport.humanreadableid.model.interfaces.Prefix;

/**
 * Форматтер идеентификатора.
 */

public interface HumanReadbaleIdFormatter {

    /**
     * Форматирование ЧЧИ.
     *
     * @param prefix префикс.
     * @param organizationId корневой идентификатор.
     * @param sequenceValue значение последовательности.
     *
     * @return сформатированый ЧЧИ.
     */
    String format(Prefix prefix, Long organizationId, Long sequenceValue);
}
