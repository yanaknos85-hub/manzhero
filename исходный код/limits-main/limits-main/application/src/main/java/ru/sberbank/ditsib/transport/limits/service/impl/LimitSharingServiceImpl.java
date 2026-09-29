package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmpLimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitNotSufficientException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LimitSharingServiceImpl implements LimitSharingService {

    private static final String LIMIT_LOGIC_EXCEPTION_TEXT =
            "createSharingPerPeriod: ERROR: LimitSharingPerPeriod already exists for limit sharing %s and period %s";
    private final List<LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;
    private final EmpLimitRepository empLimitRepository;

    private final DepLimitRepository depLimitRepository;

    private final LimitRepository<?> limitRepository;

    private final LimitTransferHistoryService limitTransferHistoryService;

    private final LimitHistoryService limitHistoryService;

    private final LimitSharingRepository limitSharingRepository;

    private final List<LimitsSharingDistributor<? extends Period>> limitsSharingDistributors;

    private final Clock clock;

    @Override
    @Transactional
    public LimitSharing add(LimitSharing limitSharing) {
        limitSharing.setCreationTime(LocalDateTime.now(clock));
        LimitSharing result;
        try {
            result = limitSharingRepository.save(limitSharing);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }

    @Override
    @Transactional
    public LimitSharing add(Limit limit, TransportTypeEnum transportType, Employee author, BigDecimal sum, BigDecimal balance) {
        // and create limit sharing for it
        var targetLimitSharing = new LimitSharing();
        targetLimitSharing.setSum(sum);
        targetLimitSharing.setBalance(balance);
        targetLimitSharing.setTransportType(transportType);
        targetLimitSharing.setLimit(limit);
        targetLimitSharing.setAuthor(author);
        targetLimitSharing.setCreationTime(LocalDateTime.now(clock));
        targetLimitSharing = add(targetLimitSharing);
        return targetLimitSharing;
    }

    @Override
    @Transactional
    public LimitSharing getLimitSharing(Limit limit, TransportTypeEnum transportType, Employee author) {
        var limitSharing = getByLimitAndTransportType(limit, transportType);
        if (limitSharing == null) {
            limitSharing = add(limit, transportType, author, BigDecimal.ZERO, BigDecimal.ZERO);
            log.info("LIMITS: getLimitSharing: limit sharing was added for limit {} transportType {}", limit.getId(), transportType.name());
        }
        if (LimitStatus.SHARED.equals(limit.getLimitStatus()) && !limitSharing.isDistributed()) {
            createAndDistributeSharingPerPeriod(limitSharing, author);
        }
        return limitSharing;
    }

    @Override
    @Transactional
    public void save(LimitSharing limitSharing) {
        limitSharingRepository.save(limitSharing);
    }

    @Override
    @Transactional
    public void delete(LimitSharing limitSharing) {
        limitSharingRepository.delete(limitSharing);
    }

    @Override
    public Optional<LimitSharing> get(UUID id) {
        return limitSharingRepository.findById(id);
    }

    @Override
    public List<LimitSharing> getAll() {
        return limitSharingRepository.findAll();
    }

    public Page<LimitSharing> getAll(Integer page, Integer size, Sort.Direction direction, UUID limitId) {
        var pageRequest = PageRequest.of(page, size, direction, LimitSharing_.CREATION_TIME);
        return limitSharingRepository.findAll((root, query, cb) -> {
            var predicate = cb.isTrue(cb.literal(true));
            if (limitId != null) {
                var limit = root.join(LimitSharing_.limit);
                predicate = cb.equal(limit.get(Limit_.id), limitId);
            }
            return predicate;
        },pageRequest);
    }

    @Override
    public List<LimitSharing> getByLimit(Limit limit) {
        return limitSharingRepository.findByLimit(limit);
    }

    @Override
    public List<LimitSharing> getByLimitIds(List<UUID> limitIds) {
        return limitSharingRepository.findByLimitIdIn(limitIds);
    }

    @Override
    public LimitSharing getByLimitAndTransportType(Limit limit, TransportTypeEnum transportType) {
        return limitSharingRepository.findByLimitAndTransportType(limit, transportType).orElse(null);
    }

    private void createSharingPerPeriod(LimitSharing limitSharing, Employee author) {
        if (limitSharing == null) {
            throw new LimitLogicException("Limitsharing cannot be null");
        }
        var type = limitSharing.getLimit().getLimitSharingType();
        var limitSharingPerPeriodService = this.getLimitSharingService(type.getPeriodClass());

        for (var period : type.getPeriods()) {
            var limitSharingPerPeriodOld = limitSharingPerPeriodService.getByLimitSharingAndPeriod(limitSharing, ReflectionUtils.cast(period));
            if (limitSharingPerPeriodOld != null) {
                throw new LimitLogicException(LIMIT_LOGIC_EXCEPTION_TEXT.formatted(limitSharing.getId(), period));
            }
        }
        for (var period : type.getPeriods()) {
            limitSharingPerPeriodService.add(limitSharing, author, BigDecimal.ZERO, ReflectionUtils.cast(period));
        }
    }

    @Override
    @Transactional
    public void distributeSharingPerPeriod(LimitSharing limitSharing) {
        if (limitSharing == null) {
            throw new LimitLogicException("Limitsharing cannot be null");
        }
        var limit = limitRepository.findById(limitSharing.getLimit().getId()).orElseThrow();
        var type = limit.getLimitSharingType();
        var limitSharingPerMonthService = getLimitSharingService(type.getPeriodClass());

        var limitSharingPerPeriodList = limitSharingPerMonthService.getByLimitSharing(limitSharing);
        if (limitSharingPerPeriodList.size() != type.getPeriods().size()) {
            for (var period : type.getPeriods()) {
                if (!limitSharingPerPeriodList.containsKey(period)) {
                    var value = new LimitSharingPerPeriod();
                    value.setPeriod(period);
                    value.setLimitSharing(limitSharing);
                    value.setAuthor(limitSharing.getAuthor());
                    value.setAdditionalSum(BigDecimal.ZERO);
                    limitSharingPerPeriodList.put(value.getPeriod(), value);
                }
            }
        }
        int currentYear = LocalDate.now(clock).getYear();
        if (currentYear == limit.getYear()) {
            distributeLimitSharingCurrentYear(limitSharing.getLimit(),
                    limitSharing.getBalance(),
                    limitSharingPerPeriodList,
                    null, false);
        } else {
            distributeSharingPerPeriodOtherYears(limit,
                    limitSharing.getBalance(),
                    limitSharingPerPeriodList,
                    null, false);
        }
    }

    @Override
    @Transactional
    public Map<Period, BigDecimal> distributeSharingPerPeriodImitation(Limit limit, BigDecimal totalSum) {
        var sumMap = new HashMap<Period, BigDecimal>();

        int currentYear = LocalDate.now(clock).getYear();
        if (currentYear == limit.getYear()) {
            distributeLimitSharingCurrentYear(limit,
                    totalSum,
                    null,
                    sumMap, true);
        } else {
            distributeSharingPerPeriodOtherYears(limit,
                    totalSum,
                    null,
                    sumMap, true);
        }
        for (var month : Month.values()) {
            sumMap.putIfAbsent(month, BigDecimal.ZERO);
        }
        return sumMap;
    }

    private void distributeSharingPerPeriodOtherYears(Limit limit,
                                                      BigDecimal totalSum,
                                                      Map<? extends Period, LimitSharingPerPeriod> limitSharingPerPeriodList,
                                                      Map<Period, BigDecimal> sumMap,
                                                      boolean imitation
    ) {
        getLimitSharingDistributor(limit.getLimitSharingType())
                .distributeSharingPerPeriodOtherYears(limit, totalSum, limitSharingPerPeriodList, sumMap, imitation);
    }

    private void distributeLimitSharingCurrentYear(Limit limit,
                                                   BigDecimal totalSum,
                                                   Map<? extends Period, LimitSharingPerPeriod> limitSharingPerPeriodListFull,
                                                   Map<Period, BigDecimal> sumMap,
                                                   boolean imitation) {
        getLimitSharingDistributor(limit.getLimitSharingType())
                .distributeLimitSharingCurrentYear(limit, totalSum, limitSharingPerPeriodListFull, sumMap, imitation);
    }

    @Override
    @Transactional
    public void moveRemainsToNextPeriod(LimitSharing limitSharing, Period period) {
        var limitSharingService = getLimitSharingService(period.getClass());

        var limitSharingPerPeriodListFull = limitSharingService.getByLimitSharing(limitSharing);
        var nextPeriod = period.next();
        var source = limitSharingPerPeriodListFull.get(period);
        var target = limitSharingPerPeriodListFull.get(nextPeriod);
        // move money
        final var sum = source.getBalance();
        source.setSum(source.getSum().subtract(sum));
        source.setBalance(BigDecimal.ZERO);
        target.setSum(target.getSum().add(sum));
        target.setBalance(target.getBalance().add(sum));
        limitSharingService.save(source);
        limitSharingService.save(target);

        var authorId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        var sourceData = new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), period);
        var targetData = new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), nextPeriod);

        limitTransferHistoryService.add(authorId, sourceData, targetData, sum, limitSharing.getLimit().getYear(),
                LimitTransferHistoryType.GENERAL);

        limitHistoryService.add(authorId,
                new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), period),
                new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), nextPeriod),
                sum, limitSharing.getLimit().getYear(),
                LimitHistoryType.TRANSFER_INSIDE_LIMIT_BETWEEN_PERIODS,
                source.getLimitSharing(),
                source);
        limitHistoryService.add(authorId,
                new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), nextPeriod),
                new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), period),
                sum, limitSharing.getLimit().getYear(),
                LimitHistoryType.TRANSFER_INSIDE_LIMIT_BETWEEN_PERIODS,
                source.getLimitSharing(),
                source);
    }

    @Override
    @Transactional
    public void moveRemainsToEconomy(LimitSharing limitSharing, Period period) {
        var limitSharingPerMonthService = getLimitSharingService(period.getClass());

        var limitSharingPerPeriod = limitSharingPerMonthService.getForPeriod(limitSharing, ReflectionUtils.cast(period));
        var mainLimit = getUpperParent(limitSharing.getLimit());
        final var sum = limitSharingPerPeriod.getBalance();

        // minus sum
        limitSharingPerPeriod.setSum(limitSharingPerPeriod.getSum().subtract(sum));
        limitSharingPerPeriod.setBalance(BigDecimal.ZERO);
        changeLimitSharingSumAndBalance(limitSharing, sum.negate());
        changeLimitSum(limitSharing.getLimit(), sum.negate());
        limitSharingPerMonthService.save(limitSharingPerPeriod);

        // plus sum
        mainLimit.setEconomy(mainLimit.getEconomy().add(sum));
        depLimitRepository.save(mainLimit);

        var authorId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        var sourceData = new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), period);
        var targetData = new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), null);

        limitTransferHistoryService.add(authorId, sourceData,targetData, sum, limitSharing.getLimit().getYear(),
                LimitTransferHistoryType.TO_ECONOMY);

        limitHistoryService.add(authorId,
                new LimitData(limitSharing.getLimit(), limitSharing.getTransportType(), period),
                new LimitData(mainLimit, limitSharing.getTransportType(), null),
                sum, limitSharing.getLimit().getYear(),
                LimitHistoryType.TRANSFER_TO_ECONOMY,
                limitSharing,
                limitSharingPerPeriod);
    }

    @Override
    @Transactional
    public void changeLimitSharingSumAndBalance(LimitSharing limitSharing, BigDecimal sum) {
        limitSharing.setSum(limitSharing.getSum().add(sum));
        limitSharing.setBalance(limitSharing.getBalance().add(sum));
        save(limitSharing);
    }

    @Override
    public LimitSharingPerPeriod putToMonth(LimitSharing limitSharing, BigDecimal sum, Period period) {
        // get limit sharing per period for this month
        var limitSharingPerMonthService = getLimitSharingService(period.getClass());
        var limitSharingPerPeriod = limitSharingPerMonthService.getForPeriod(limitSharing, ReflectionUtils.cast(period));
        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            if (limitSharingPerPeriod.getSum().compareTo(sum.abs()) < 0) {
                throw new LimitNotSufficientException(limitSharing.getLimit().getHumanReadableId(),
                        limitSharing.getTransportType());
            }
            if (limitSharingPerPeriod.getBalance().compareTo(sum.abs()) < 0) {
                throw new LimitNotSufficientException(limitSharing.getLimit().getHumanReadableId(),
                        limitSharing.getTransportType());
            }
        }
        // put money to this limitSharingPerPeriod
        limitSharingPerPeriod.setSum(limitSharingPerPeriod.getSum().add(sum));
        limitSharingPerPeriod.setBalance(limitSharingPerPeriod.getBalance().add(sum));
        limitSharingPerMonthService.save(limitSharingPerPeriod);
        return limitSharingPerPeriod;
    }

    @Override
    @Transactional
    public void createAndDistributeSharingPerPeriod(LimitSharing limitSharing, Employee author) {
        if (limitSharing != null) {
            if (!limitSharing.isDistributed()) {
                createSharingPerPeriod(limitSharing, author);
                distributeSharingPerPeriod(limitSharing);
                limitSharing.setDistributed(true);
                save(limitSharing);
            } else {
                throw new LimitLogicException("createAndDistributeSharingPerPeriod: ERROR: " +
                        "LimitSharingPerPeriod allready distributed " +
                        "for limitsharing " +
                        limitSharing.getId());
            }
        }
    }

    private LimitsSharingDistributor<Period> getLimitSharingDistributor(@NonNull LimitSharingType limitSharingType) {
        var distributor = limitsSharingDistributors.stream()
                .filter(limitsSharingDistributor -> limitSharingType.equals(limitsSharingDistributor.type()))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanDefinitionException("No distributor for %s found".formatted(limitSharingType)));
        return ReflectionUtils.cast(distributor);
    }

    private <T extends Period> LimitSharingPerPeriodService<T> getLimitSharingService(Class<? super T> periodClass) {
        var type = ResolvableType.forClassWithGenerics(LimitSharingPerPeriodService.class, periodClass);
        var service = limitSharingPerPeriodServices.stream().filter(type::isInstance).findFirst()
                .orElseThrow(() -> new NoSuchBeanDefinitionException("No limit sharing service for %s found".formatted(periodClass.getSimpleName())));
        return ReflectionUtils.cast(service);
    }

    private DepLimit getUpperParent(Limit limit) {
        var parentLimit = limit;
        while (parentLimit.getParent() != null) {
            parentLimit = parentLimit.getParent();
        }
        return (DepLimit) parentLimit;
    }

    /**
     * Change limit sum for limit.
     */
    private void changeLimitSum(Limit limit, BigDecimal sum) {
        if (limit.getParent() != null) {
            limit.setSum(limit.getSum().add(sum));
            if (limit instanceof EmpLimit empLimit) {
                empLimitRepository.save(empLimit);
            }
            if (limit instanceof DepLimit depLimit) {
                depLimitRepository.save(depLimit);
            }
        }
    }

}
