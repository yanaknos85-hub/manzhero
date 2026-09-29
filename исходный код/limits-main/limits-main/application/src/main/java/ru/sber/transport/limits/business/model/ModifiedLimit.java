package ru.sber.transport.limits.business.model;

/**
 * Дата об изменении лимита
 *
 * @param modified признак того, что лимит был изменен
 * @param data данные лимита
 */
public record ModifiedLimit(
        boolean modified,
        Limit data
) implements Modified<Limit> {
}
