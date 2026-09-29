package ru.sberbank.ditsib.transport.limits.human_readable_id.service;

import org.springframework.stereotype.Component;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.SQCreator;
import ru.sberbank.ditsib.transport.limits.human_readable_id.model.CompanySQLimits;

/**
 * Implementation for module limits
 */
@Component
public class SQCreatorImpl implements SQCreator<CompanySQLimits> {
    /**
     * Create concrete instance of BaseCompanySQ
     *
     * @param prefix .
     * @param organizationId ID
     *
     * @return instance
     */
    @Override
    public CompanySQLimits createCompanySQ(Prefix prefix, Long organizationId, int count) {
        return CompanySQLimits.builder().prefix(prefix.name()).orgDigitId(organizationId).sq((long) count).build();
    }
}
