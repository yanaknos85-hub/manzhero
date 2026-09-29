package ru.sber.transport.humanreadableid.service;

import ru.sber.transport.humanreadableid.model.interfaces.Prefix;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Генератор последовательности.
 */
public interface SQGenerator {

    /**
     * Генерация идентификатора в формате XX-XXXX-X....
     *
     * @param prefix префикс для генерации
     * @param organizationId корневой идентификатор
     *
     * @return идентификатор
     */
    String getNextId(@NotNull Prefix prefix, @Min(1) @Max(9999) Long organizationId);

    /**
     * Резервирование пула человекочитаемых идентификаторов.
     *
     * @param prefix префикс.
     * @param organizationId идентификатор организации.
     * @param count количество необходимых идентификаторов.
     * @return идентификаторы.
     */
    Iterable<String> reserve(@NotNull Prefix prefix, @Min(1) @Max(9999) Long organizationId, int count);
}
