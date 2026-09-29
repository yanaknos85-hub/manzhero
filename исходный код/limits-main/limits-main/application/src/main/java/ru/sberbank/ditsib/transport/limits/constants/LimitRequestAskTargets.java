package ru.sberbank.ditsib.transport.limits.constants;

/**
 * Status of upload
 */
public enum LimitRequestAskTargets {

    /**
     * Return limit to parent.
     */
    PARENT,

    /**
     * Send limit to siblings.
     */
    SIBLINGS
}
