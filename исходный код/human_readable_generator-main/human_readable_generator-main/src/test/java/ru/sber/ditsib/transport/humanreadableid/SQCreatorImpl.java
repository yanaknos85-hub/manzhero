package ru.sber.ditsib.transport.humanreadableid;

import org.springframework.stereotype.Component;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.SQCreator;

/**
 * Implementation for module limits
 */
@Component
public class SQCreatorImpl implements SQCreator<CompanySQRequest> {
    /**
     * Create concrete instance of BaseCompanySQ
     *
     * @param prefix .
     * @param organizationId ID
     *
     * @return instance
     */
    @Override
    public CompanySQRequest createCompanySQ(Prefix prefix, Long organizationId, int count) {
        return CompanySQRequest.builder().prefix(prefix.name()).orgDigitId(organizationId).sq((long) count).build();
    }
}
