package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPerPeriodRepository;
import ru.sberbank.ditsib.transport.limits.model.limit.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LimitSharingPerMonthServiceImpl extends BaseLimitSharingService<Month> {

    public LimitSharingPerMonthServiceImpl(LimitSharingPerPeriodRepository limitSharingPerPeriodRepository) {
        super(limitSharingPerPeriodRepository);
    }
    
    @Override
    public Month getPeriodNumber(Limit limit, LocalDate date) {
        if ((limit.getLimitSharingType() == LimitSharingType.MONTHLY) ||
            (limit.getLimitSharingType() == LimitSharingType.PERCENTS)) {
            return Month.valueOf(date.getMonth());
        }
        throw new UnsupportedOperationException("Unknown sharing type %s at the month service".formatted(limit.getLimitSharingType()));
    }

    @Override
    public LimitSharingPerPeriod getForDate(LimitSharing limitSharing, LocalDate date) {
        return getForPeriod(limitSharing, Month.valueOf(date.getMonth()));
    }

    @Override
    public Map<LimitSharing, LimitSharingPerPeriod> getPeriodsForDateBySharings(Collection<LimitSharing> sharings, LocalDate date) {
        return getPeriodsBySharings(sharings, Month.valueOf(date.getMonth()));
    }

    @Override
    public List<LimitSharingType> types() {
        return List.of(LimitSharingType.MONTHLY, LimitSharingType.PERCENTS);
    }
}
