package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPerPeriodRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
abstract class BaseLimitSharingService<T extends Period> implements LimitSharingPerPeriodService<T> {

    private final LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;

    @Override
    public LimitSharingPerPeriod getByLimitSharingAndPeriod(LimitSharing limitSharing, T period) {
        return limitSharingPerPeriodRepository.findByLimitSharingAndPeriodData(limitSharing, PeriodData.valueOf(period.name()))
                .orElse(null);
    }

    @Override
    @Transactional
    public void save(LimitSharingPerPeriod limitSharingPerPeriod) {
        limitSharingPerPeriodRepository.save(limitSharingPerPeriod);
    }

    @Override
    public Optional<LimitSharingPerPeriod> get(UUID id) {
        return limitSharingPerPeriodRepository.findById(id);
    }

    @Override
    public List<LimitSharingPerPeriod> getAll() {
        return limitSharingPerPeriodRepository.findAll();
    }

    @Override
    public Map<T, LimitSharingPerPeriod> getByLimitSharing(LimitSharing limitSharing) {
        return limitSharingPerPeriodRepository.findByLimitSharing(limitSharing)
            .stream().collect(Collectors.toMap(LimitSharingPerPeriod::getPeriod, Function.identity()));
    }

    @Override
    @Transactional
    public void delete(LimitSharingPerPeriod limitSharingPerPeriod) {
        limitSharingPerPeriodRepository.delete(limitSharingPerPeriod);
    }

    @Override
    @Transactional
    public LimitSharingPerPeriod add(
        LimitSharing limitSharing, Employee author, BigDecimal sum,
        T period
    ) {
        var limitSharingPerPeriodOld = getByLimitSharingAndPeriod(limitSharing, period);
        if (limitSharingPerPeriodOld != null) {
            throw new LimitLogicException("LimitSharingPerPeriod: add: ERROR: " +
                                          "limitSharingPerPeriod allready exists " +
                                          "for limitsharing " +
                                          limitSharing.getId() +
                                          " and period number " +
                                          period);
        }

        var limitSharingPerPeriod = new LimitSharingPerPeriod();
        limitSharingPerPeriod.setAuthor(author);
        limitSharingPerPeriod.setSum(sum);
        limitSharingPerPeriod.setBalance(sum);
        limitSharingPerPeriod.setLimitSharing(limitSharing);
        limitSharingPerPeriod.setPeriod(period);
        limitSharingPerPeriod.setAdditionalSum(BigDecimal.ZERO);
        return limitSharingPerPeriodRepository.save(limitSharingPerPeriod);
    }

    @Override
    public LimitSharingPerPeriod getForPeriod(LimitSharing limitSharing, T month) {
        var limitSharingPerPeriod = getByLimitSharingAndPeriod(limitSharing, month);
        if (limitSharingPerPeriod == null) {
            throw new LimitLogicException("Ошибка: LimitSharingPerPeriod не найден для limitSharing '"
                                          + limitSharing.getId()
                                          + "' за период "
                                          + month);
        }
        return limitSharingPerPeriod;
    }

    @Override
    public Map<LimitSharing, LimitSharingPerPeriod> getPeriodsBySharings(Collection<LimitSharing> sharings, T period) {
        if (sharings.isEmpty()) {
            return Collections.emptyMap();
        }

        var periodData = PeriodData.valueOf(period.name());
        var periods = limitSharingPerPeriodRepository.findByLimitSharingIdsInAndPeriodData(sharings.stream().map(LimitSharing::getId).toList(), periodData);

        return periods.stream().collect(Collectors.toMap(LimitSharingPerPeriod::getLimitSharing, Function.identity()));
    }

}
