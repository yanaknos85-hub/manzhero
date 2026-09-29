package ru.sber.transport.humanreadableid.service;

import ru.sber.transport.humanreadableid.model.interfaces.Prefix;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Сервис по работе с человеко-читаемыми идентификаторами.
 */
public interface CompanySQService {
    
    /**
     * Получение свободного корневого идентификатора.
     *
     * @param prefix префикс для генерации.
     * @param organizationId корневой идентификатор.
     * @param count количество необходимых идентификаторов.
     *
     * @return значение последовательности.
     */
    Long getOrCreateCompanySQ(@NotNull Prefix prefix, @Min(1) @Max(9999) Long organizationId, int count);
    
    
    /**
     * Обновление существующего идентификатора.
     *
     * @param prefix префикс для генерации.
     * @param organizationId корневой идентификатор.
     * @param count количество необходимых идентификаторов.
     *
     * @return значение последовательности.
     */
    Long getNextValue(@NotNull Prefix prefix, @Min(1) @Max(9999) Long organizationId, int count);
    
    
}