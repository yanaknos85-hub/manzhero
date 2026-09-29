package ru.sberbank.ditsib.transport.limits.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.LimitTransferHistoryRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory_;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LimitTransferHistoryServiceImpl implements LimitTransferHistoryService {
    
    private final LimitTransferHistoryRepository limitTransferHistoryRepository;
    
    @Override
    @Transactional
    @Async
    public Future<LimitTransferHistory> add(UUID authorId, LimitData source, LimitData target,
                                            BigDecimal sum, Integer year,
                                            LimitTransferHistoryType historyType) {
        var sourceLimit = source.limit();
        var targetLimit = target.limit();
        if (!sourceLimit.getOrganization().getId().equals(targetLimit.getOrganization().getId())) {
            throw new LimitLogicException("LimitTransferHistory: operation not allowed: different organizations!");
        }
        var limitTransferHistory = new LimitTransferHistory();
        limitTransferHistory.setAuthor(authorId);
        limitTransferHistory.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitTransferHistory.setSum(sum);
        limitTransferHistory.setYear(year);
        limitTransferHistory.setOrganizationId(sourceLimit.getOrganization().getId());
        limitTransferHistory.setLimitServiceType(sourceLimit.getLimitServiceType());
        limitTransferHistory.setSourceLimitId(sourceLimit.getId());
        limitTransferHistory.setSourceTransportType(source.transportType());
        limitTransferHistory.setTargetLimitId(targetLimit.getId());
        limitTransferHistory.setTargetTransportType(target.transportType());
        limitTransferHistory.setSourcePeriod(Optional.ofNullable(source.period()).map(Period::name).map(PeriodData::valueOf).orElse(null));
        limitTransferHistory.setTargetPeriod(Optional.ofNullable(target.period()).map(Period::name).map(PeriodData::valueOf).orElse(null));
        limitTransferHistory.setHistoryType(historyType);
        limitTransferHistory = save(limitTransferHistory);
        return CompletableFuture.completedFuture(limitTransferHistory);
    }
    
    @Override
    @Transactional
    public LimitTransferHistory save(LimitTransferHistory limitTransferHistory) {
        LimitTransferHistory result;
        try {
            result = limitTransferHistoryRepository.save(limitTransferHistory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public void delete(LimitTransferHistory limitTransferHistory) {
        try {
            limitTransferHistoryRepository.delete(limitTransferHistory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Optional<LimitTransferHistory> get(UUID id) {
        return limitTransferHistoryRepository.findById(id);
    }
    
    @Override
    public List<LimitTransferHistory> getAll() {
        return limitTransferHistoryRepository.findAll();
    }
    
    @Override
    public List<LimitTransferHistory> getByLimit(Integer year, UUID limitId) {
        List<LimitTransferHistory> list1 =
                limitTransferHistoryRepository.findByYearAndSourceLimitIdOrderByCreationTime(year, limitId);
        List<LimitTransferHistory> list2 =
                limitTransferHistoryRepository.findByYearAndTargetLimitIdOrderByCreationTime(year, limitId);
        List<LimitTransferHistory> listAll = new ArrayList<>();
        listAll.addAll(list1);
        listAll.addAll(list2);
        return listAll;
    }
    
    @Override
    public List<LimitTransferHistory> getByLimitAndTransportType(Integer year, UUID limitId, TransportTypeEnum transportType) {
        List<LimitTransferHistory> list1 =
                limitTransferHistoryRepository.findByYearAndSourceLimitIdAndSourceTransportTypeOrderByCreationTime(
                        year, limitId, transportType);
        List<LimitTransferHistory> list2 =
                limitTransferHistoryRepository.findByYearAndTargetLimitIdAndTargetTransportTypeOrderByCreationTime(
                        year, limitId, transportType);
        List<LimitTransferHistory> listAll = new ArrayList<>();
        listAll.addAll(list1);
        listAll.addAll(list2);
        return listAll;
    }
    
    @Override
    public List<LimitTransferHistory> getByLimitAndTransportTypeAndPeriod(Integer year, UUID limitId,
                                                                          TransportTypeEnum transportType,
                                                                          Period period) {
        var list1 =
                limitTransferHistoryRepository.findByYearAndSourceLimitIdAndSourceTransportTypeAndSourcePeriodOrderByCreationTime(
                        year, limitId, transportType, period.name());
        var list2 =
                limitTransferHistoryRepository.findByYearAndTargetLimitIdAndTargetTransportTypeAndTargetPeriodOrderByCreationTime(
                        year, limitId, transportType, period.name());
        return Stream.concat(list1.stream(), list2.stream()).toList();
    }
    
    @Override
    public List<LimitTransferHistory> getBySourceLimit(Integer year, UUID limitId) {
        return limitTransferHistoryRepository.findByYearAndSourceLimitIdOrderByCreationTime(year, limitId);
    }
    
    @Override
    public List<LimitTransferHistory> getByTargetLimit(Integer year, UUID limitId) {
        return limitTransferHistoryRepository.findByYearAndTargetLimitIdOrderByCreationTime(year, limitId);
    }
    
    @Override
    public List<LimitTransferHistory> getHistoryTransfersToEconomy(UUID organizationId, String serviceType, Integer year, Period period) {
        return limitTransferHistoryRepository.findByOrganizationIdAndLimitServiceTypeAndYearAndSourcePeriodAndHistoryType(organizationId,
                                                                                                                          serviceType, year,
                                                                                                                          PeriodData.valueOf(period.name()),
                                                                                                                          LimitTransferHistoryType.TO_ECONOMY);
    }
    
    @Override
    public List<LimitTransferHistory> getHistoryTransfersFromEconomy(UUID limitId, Integer year) {
        return limitTransferHistoryRepository.findByYearAndHistoryType(limitId, year, LimitTransferHistoryType.FROM_ECONOMY);
    }

    @Override
    public Page<LimitTransferHistory> getAll(UUID organizationId, UUID limitId, Integer year, int page, int size, Sort.Direction direction) {
        var sort = Sort.by(LimitTransferHistory_.CREATION_TIME);
        var pageRequest = PageRequest.of(page, size, Sort.Direction.ASC.equals(direction) ? sort.ascending() : sort.descending());
        return limitTransferHistoryRepository.findAll(
                (root, query, cb) -> {
                    var predicate = cb.equal(cb.literal(true), true);
                    predicate = append(predicate, cb, organizationId, root.get(LimitTransferHistory_.organizationId));
                    predicate = append(predicate, cb, limitId, root.get(LimitTransferHistory_.sourceLimitId), root.get(LimitTransferHistory_.targetLimitId));
                    predicate = append(predicate, cb, year, root.get(LimitTransferHistory_.year));
                    return predicate;
                },
                pageRequest);
    }

    private <T> Predicate append(Predicate predicate, CriteriaBuilder cb, T value, Path<? super T> valuePath) {
        if (value != null) {
            predicate = cb.and(predicate, cb.equal(valuePath, value));
        }
        return predicate;
    }

    private <T> Predicate append(Predicate predicate, CriteriaBuilder cb, T value, Path<? super T> valuePath, Path<? super T> orPath) {
        if (value != null) {
            predicate = cb.and(predicate, cb.or(cb.equal(valuePath, value), cb.equal(orPath, value)));
        }
        return predicate;
    }

}
