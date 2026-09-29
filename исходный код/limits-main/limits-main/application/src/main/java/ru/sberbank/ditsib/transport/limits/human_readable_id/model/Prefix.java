package ru.sberbank.ditsib.transport.limits.human_readable_id.model;

/**
 * Prefix for generating human readable IDs.
 */
public enum Prefix implements ru.sber.transport.humanreadableid.model.interfaces.Prefix {

    /**
     * Department limit.
     */
    LD,

    /**
     * Limit request.
     */
    OL,

    /**
     * User limit.
     */
    LU
}
