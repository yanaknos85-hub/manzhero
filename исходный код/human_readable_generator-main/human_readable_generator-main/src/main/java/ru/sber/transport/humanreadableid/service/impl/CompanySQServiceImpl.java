package ru.sber.transport.humanreadableid.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;
import ru.sber.transport.humanreadableid.exceptions.CannotCreateSequenceException;
import ru.sber.transport.humanreadableid.model.BaseCompanySQ;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.SQCreator;

/**
 * Implementation of service for working with human readable id
 * @param <T> тип последовательности.
 */
@Component
@Validated
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CompanySQServiceImpl<T extends BaseCompanySQ> implements CompanySQService {

    private final AbstractRepository<T> companySQRepository;

    private final SQCreator<T> sqCreator;
    
    @Override
    public Long getOrCreateCompanySQ(@NonNull Prefix prefix, @NonNull Long organizationId, int count) {
        var companySQ = companySQRepository.findByPrefixAndOrgDigitId(prefix.name(), organizationId);
        if (companySQ == null) {
            companySQ = sqCreator.createCompanySQ(prefix, organizationId, count);
            companySQRepository.saveAndFlush(companySQ);
            return 0L;
        } else {
            var start = companySQ.getSq();
            companySQ.setSq(start + count);
            companySQRepository.saveAndFlush(companySQ);
            return start;
        }
    }
    
    
    @Override
    public Long getNextValue(@NonNull Prefix prefix, @NonNull Long organizationId, int count) {
        var prefixName = prefix.name();
        var companySQ = companySQRepository.findByPrefixAndOrgDigitIdForWrite(prefixName, organizationId);
        if (companySQ != null) {
            companySQ.setSq(companySQ.getSq() + count);
            companySQRepository.saveAndFlush(companySQ);
        } else {
            throw new CannotCreateSequenceException();
        }
        return companySQ.getSq();
    
    }
}
