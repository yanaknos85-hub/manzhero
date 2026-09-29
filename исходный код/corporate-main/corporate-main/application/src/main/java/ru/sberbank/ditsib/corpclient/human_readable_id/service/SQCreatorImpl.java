package ru.sberbank.ditsib.corpclient.human_readable_id.service;

import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.CompanySQ;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.SQCreator;

/**
 * Implementation for module limits
 */
@Service
public class SQCreatorImpl implements SQCreator<CompanySQ> {
    /**
     * Create concrete instance (CompanySQEmployee) of BaseCompanySQ
     *
     * @param prefix .
     * @param organizationId ID
     *
     * @return instance
     */
    @Override
    public CompanySQ createCompanySQ(Prefix prefix, Long organizationId, int count) {
        return CompanySQ.builder().prefix(prefix.name()).orgDigitId(organizationId).sq((long) count).build();
    }
}
