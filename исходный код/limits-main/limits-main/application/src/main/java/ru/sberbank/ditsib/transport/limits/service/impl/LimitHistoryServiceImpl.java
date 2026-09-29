package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.LimitHistoryRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitHistoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LimitHistoryServiceImpl implements LimitHistoryService {
    
    private final LimitHistoryRepository limitHistoryRepository;
    
    @Override
    @Transactional
    @Async
    public Future<LimitHistory> add(UUID authorId,
                                    LimitData limitData,
                                    LimitData counterLimitData,
                                    BigDecimal sum, Integer year,
                                    LimitHistoryType historyType,
                                    LimitSharing limitSharing,
                                    LimitSharingPerPeriod limitSharingPerPeriod) {
        var limit = limitData.limit();
        var counterpartLimit = Optional.ofNullable(counterLimitData).map(LimitData::limit).orElse(null);
        if (counterpartLimit != null &&
            !limit.getOrganization().getId().equals(counterpartLimit.getOrganization().getId())) {
            throw new LimitLogicException("LimitHistory: operation not allowed: different organizations!");
        }
        
        var limitHistory = new LimitHistory();
        limitHistory.setAuthorId(authorId);
        limitHistory.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitHistory.setSum(sum);
        limitHistory.setYear(year);
        limitHistory.setOrganizationId(limit.getOrganization().getId());
        limitHistory.setLimitServiceType(limit.getLimitServiceType());
        limitHistory.setLimitId(limit.getId());
        limitHistory.setTransportType(limitData.transportType());
        limitHistory.setPeriod(Optional.ofNullable(limitData.period()).map(Period::name).map(PeriodData::valueOf).orElse(null));
        limitHistory.setCounterpartLimitId(counterpartLimit != null ? counterpartLimit.getId() : null);
        limitHistory.setCounterpartTransportType(Optional.ofNullable(counterLimitData).map(LimitData::transportType).orElse(null));
        limitHistory.setCounterpartPeriod(Optional.ofNullable(counterLimitData).map(LimitData::period).map(Period::name).map(PeriodData::valueOf).orElse(null));
        limitHistory.setHistoryType(historyType);
        limitHistory.setLimitSum(limit.getSum());
        
        if (limitSharing != null) {
            limitHistory.setLimitSharingSum(limitSharing.getSum());
            limitHistory.setLimitSharingBalance(limitSharing.getBalance());
        }
        if (limitSharingPerPeriod != null) {
            limitHistory.setLimitSharingPerPeriodSum(limitSharingPerPeriod.getSum());
            limitHistory.setLimitSharingPerPeriodBalance(limitSharingPerPeriod.getBalance());
        }
        
        limitHistory = save(limitHistory);

        return CompletableFuture.completedFuture(limitHistory);
    }
    
    @Override
    @Transactional
    public LimitHistory save(LimitHistory limitHistory) {
        LimitHistory result;
        try {
            result = limitHistoryRepository.save(limitHistory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public void delete(LimitHistory limitHistory) {
        try {
            limitHistoryRepository.delete(limitHistory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Optional<LimitHistory> get(UUID id) {
        return limitHistoryRepository.findById(id);
    }
    
    @Override
    public List<LimitHistory> getAll() {
        return limitHistoryRepository.findAll();
    }

    @Override
    public Page<LimitHistory> getAll(UUID limitId, int page, int size, Sort.Direction direction) {
        var pageRequest = PageRequest.of(page, size, direction, LimitHistory_.CREATION_TIME);
        return limitHistoryRepository.findAll((root, q, cb) -> {
            var predicate = cb.isTrue(cb.literal(true));
            if (limitId != null) {
                predicate = cb.equal(root.get(LimitHistory_.limitId), limitId);
            }
            return predicate;
        }, pageRequest);
    }

    @Override
    public List<LimitHistory> getByLimit(UUID limitId) {
        return limitHistoryRepository.findByLimitIdOrderByCreationTime(limitId);
    }
    
    @Override
    public List<LimitHistory> getByLimitAndTransportType(UUID limitId, TransportTypeEnum transportType) {
        return limitHistoryRepository.findByLimitIdAndTransportTypeOrderByCreationTime(limitId, transportType);
    }
    
    @Override
    public List<LimitHistory> getByLimitAndTransportTypeAndPeriod(UUID limitId,
                                                                          TransportTypeEnum transportType,
                                                                          Period period) {
        return limitHistoryRepository.findByLimitIdAndTransportTypeAndPeriodOrderByCreationTime(limitId, transportType, period.name());
    }
    

    
}
