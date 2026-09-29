package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPercentsRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPercentService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LimitSharingProcentsServiceImpl implements LimitSharingPercentService {
    
    private final LimitSharingPercentsRepository limitSharingPercentsRepository;
    
    @Override
    @Transactional
    public LimitSharingPercents add(final LimitSharingPercents limitSharingPercents) {
        limitSharingPercents.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        LimitSharingPercents result;
        if (isLimitSharingNotAvailable(limitSharingPercents)) {
            throw new LimitLogicException("Общая сумма процентов должна быть равна 100");
        }
        try {
            result = limitSharingPercentsRepository.save(limitSharingPercents);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public void save(LimitSharingPercents limitSharingPercents) {
        if (isLimitSharingNotAvailable(limitSharingPercents)) {
            throw new LimitLogicException("Общая сумма процентов должна быть равна 100");
        }
        try {
            limitSharingPercentsRepository.save(limitSharingPercents);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void delete(LimitSharingPercents limitSharingPercents) {
        try {
            limitSharingPercentsRepository.delete(limitSharingPercents);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Optional<LimitSharingPercents> get(UUID id) {
        return limitSharingPercentsRepository.findById(id);
    }
    
    @Override
    public List<LimitSharingPercents> getAll() {
        return limitSharingPercentsRepository.findAll();
    }

    @Override
    public Page<LimitSharingPercents> getAll(Integer page, Integer size, Sort.Direction direction, UUID limitId) {
        var pageRequest = PageRequest.of(page, size, direction, LimitSharingPercents_.CREATION_TIME);
        return limitSharingPercentsRepository.findAll((root, query, cb) -> {
            var predicate = cb.isTrue(cb.literal(true));
            if (limitId != null) {
                var limit = root.join(LimitSharingPercents_.limit);
                predicate = cb.equal(limit.get(Limit_.id), limitId);
            }
            return predicate;
        }, pageRequest);
    }

    @Override
    public Optional<LimitSharingPercents> getByLimit(Limit limit) {
        return limitSharingPercentsRepository.findByLimit(limit);
    }
    
    /**
     * Check is limit sharing available.
     *
     * @param limitSharingPercents limit Sharing Procents object.
     *
     * @return true if percents sum up to 100
     */
    private boolean isLimitSharingNotAvailable(LimitSharingPercents limitSharingPercents) {
        int totalProcents = 0;
        for (var month : Month.values()) {
            totalProcents += month.getPercentsFunction().apply(limitSharingPercents);
        }
        return totalProcents < 100;
    }
    
}
