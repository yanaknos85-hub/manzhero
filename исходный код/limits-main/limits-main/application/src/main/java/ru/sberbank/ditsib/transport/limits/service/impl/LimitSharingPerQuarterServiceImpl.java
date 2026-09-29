package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPerPeriodRepository;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Quarter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LimitSharingPerQuarterServiceImpl extends BaseLimitSharingService<Quarter> {

    public LimitSharingPerQuarterServiceImpl(LimitSharingPerPeriodRepository limitSharingPerPeriodRepository) {
        super(limitSharingPerPeriodRepository);
    }
    
    @Override
    public Quarter getPeriodNumber(Limit limit, LocalDate date) {
        if (limit.getLimitSharingType() == LimitSharingType.QUARTER) {
            return Quarter.valueOf(date.getMonthValue() / 3);
        }
        throw new UnsupportedOperationException("Unknown sharing type %s at the quarter service".formatted(limit.getLimitSharingType()));
    }
    
    @Override
    public LimitSharingPerPeriod getForDate(LimitSharing limitSharing, LocalDate date) {
        return getForPeriod(limitSharing, Quarter.valueOf(date.getMonthValue() / 3));
    }

    @Override
    public Map<LimitSharing, LimitSharingPerPeriod> getPeriodsForDateBySharings(Collection<LimitSharing> sharings, LocalDate date) {
        return getPeriodsBySharings(sharings, Quarter.valueOf(date.getMonthValue() / 3));
    }

    @Override
    public List<LimitSharingType> types() {
        return List.of(LimitSharingType.QUARTER);
    }
    
}
