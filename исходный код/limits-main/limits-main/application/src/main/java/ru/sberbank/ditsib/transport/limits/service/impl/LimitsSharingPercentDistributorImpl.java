package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPercentService;
import ru.sberbank.ditsib.transport.limits.service.LimitsSharingDistributor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
class LimitsSharingPercentDistributorImpl implements LimitsSharingDistributor<Month> {

    private final LimitSharingPerPeriodService<Month> limitSharingPerPeriodService;

    private final LimitSharingPercentService limitSharingPercentsService;

    @Override
    public void distributeSharingPerPeriodOtherYears(Limit limit, BigDecimal totalSum, Map<? extends Month, LimitSharingPerPeriod> limitSharingPerPeriodList, Map<Month, BigDecimal> sumMap, boolean imitation) {
        DepLimit upperLevelLimit = getUpperParent(limit);
        Optional<LimitSharingPercents> limitSharingPercents = limitSharingPercentsService.getByLimit(upperLevelLimit);
        if (limitSharingPercents.isEmpty()) {
            throw new LimitLogicException("distributeSharingPerPeriodOtherYears: Таблица процентов распределения не найдена!");
        }
        var incrementedSum = BigDecimal.ZERO;

        for (var month : Month.values()) {
            BigDecimal sum;
            if (month.ordinal() < 11) {
                sum = totalSum.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(limitSharingPercents.get().getPercent(month)));
                incrementedSum = incrementedSum.add(sum);
            } else {
                sum = totalSum.subtract(incrementedSum);
            }
            if (imitation) {
                sumMap.put(month, sum);
            } else {
                setLimitSharingPerPeriodSumAndBalance(limitSharingPerPeriodList.get(month), sum);
            }
        }
    }

    @Override
    public void distributeLimitSharingCurrentYear(Limit limit, BigDecimal totalSum, Map<? extends Month, LimitSharingPerPeriod> limitSharingPerPeriodListFull, Map<Month, BigDecimal> sumMap, boolean imitation) {
        var currentMonth = Month.valueOf(LocalDate.now(ZoneOffset.UTC).getMonth());
        int numOfPeriods = Month.values().length - currentMonth.ordinal();

        DepLimit upperLevelLimit = getUpperParent(limit);
        Optional<LimitSharingPercents> limitSharingPercents = limitSharingPercentsService.getByLimit(upperLevelLimit);
        if (limitSharingPercents.isEmpty()) {
            throw new LimitLogicException("distributeLimitSharingCurrentYear: Таблица процентов распределения не найдена!");
        }

        // redistribute percents in proportion to left months
        var limitSharingPercentsNew = new LimitSharingPercents();
        var totalPercentsLeft = 0;
        for (int i = currentMonth.getValue(); i <= 12; i++) {
            totalPercentsLeft += limitSharingPercents.get().getPercent(Month.valueOf(i));
        }
        if (totalPercentsLeft > 0) {
            for (int i = currentMonth.getValue(); i <= 12; i++) {
                int newPercent = (100 * limitSharingPercents.get().getPercent(Month.valueOf(i))) / totalPercentsLeft;
                limitSharingPercentsNew.setPercent(Month.valueOf(i), newPercent);
            }
        }

        // reshare sum
        var limitSharingPerPeriodList = getSubList(limitSharingPerPeriodListFull, currentMonth, numOfPeriods,
                Month.values().length);
        var incrementedSum = BigDecimal.ZERO;

        for (var i = 0; i < numOfPeriods; i++) {
            BigDecimal sum;
            if (i < (numOfPeriods - 1)) {
                sum = totalSum.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(limitSharingPercentsNew.getPercent(limitSharingPerPeriodList.get(Month.values()[i]).getPeriod())));
                incrementedSum = incrementedSum.add(sum);
            } else {
                sum = totalSum.subtract(incrementedSum);
            }
            if (imitation) {
                sumMap.put(Month.values()[i + currentMonth.ordinal()], sum);
            } else {
                setLimitSharingPerPeriodSumAndBalance(limitSharingPerPeriodList.get(Month.values()[i]), sum);
            }
        }
    }

    @Override
    public LimitSharingType type() {
        return LimitSharingType.PERCENTS;
    }

    /**
     * Sets new sum and balance.
     *
     * @param limitSharingPerPeriod limit Sharing Per Period.
     * @param sum                   sum
     */
    private void setLimitSharingPerPeriodSumAndBalance(LimitSharingPerPeriod limitSharingPerPeriod, BigDecimal sum) {
        limitSharingPerPeriod.setSum(sum);
        limitSharingPerPeriod.setBalance(sum);
        limitSharingPerPeriodService.save(limitSharingPerPeriod);
    }

    private DepLimit getUpperParent(Limit limit) {
        var parentLimit = limit;
        while (parentLimit.getParent() != null) {
            parentLimit = parentLimit.getParent();
        }
        return (DepLimit) parentLimit;
    }

    private Map<? extends Month, LimitSharingPerPeriod> getSubList(
            Map<? extends Month, LimitSharingPerPeriod> limitSharingPerPeriodListFull,
            Month currentMonth, int numOfPeriods, int totalPeriods
    ) {
        var months = limitSharingPerPeriodListFull.keySet().stream().sorted().skip(currentMonth.ordinal()).limit(totalPeriods).toList();
        if (months.size() != numOfPeriods) {
            String sb = "distributeLimitSharingCurrentYear: getSubList: MONTHLY currentMonth: %s numOfPeriods: %d totalPeriods: %d"
                    .formatted(currentMonth.name(), numOfPeriods, totalPeriods);
            throw new LimitLogicException(sb);
        }
        return limitSharingPerPeriodListFull.entrySet().stream().filter(entry -> months.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
