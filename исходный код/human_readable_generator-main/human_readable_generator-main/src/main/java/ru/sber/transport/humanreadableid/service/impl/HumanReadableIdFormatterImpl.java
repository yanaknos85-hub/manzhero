package ru.sber.transport.humanreadableid.service.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;

/**
 * Implementation of formatter
 */
@Component
public class HumanReadableIdFormatterImpl implements HumanReadbaleIdFormatter {

    /**
     * Format
     *
     * @param prefix .
     * @param organizationId .
     * @param sequenceValue value for input to format
     *
     * @return AA-XXXX-XXXXXXXX
     */
    @Override
    public String format(Prefix prefix, Long organizationId, Long sequenceValue) {
        return String.format("%s-%04d-%08d", prefix, organizationId, sequenceValue);
    }
}
