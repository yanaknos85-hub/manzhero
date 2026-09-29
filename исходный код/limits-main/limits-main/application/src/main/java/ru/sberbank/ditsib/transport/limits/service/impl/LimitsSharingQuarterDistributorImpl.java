package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Quarter;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;
import ru.sberbank.ditsib.transport.limits.service.LimitsSharingDistributor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
class LimitsSharingQuarterDistributorImpl implements LimitsSharingDistributor<Quarter> {

    private final LimitSharingPerPeriodService<Quarter> limitSharingPerPeriodService;

    @Override
    public void distributeSharingPerPeriodOtherYears(Limit limit, BigDecimal totalSum, Map<? extends Quarter, LimitSharingPerPeriod> limitSharingPerPeriodList, Map<Quarter, BigDecimal> sumMap, boolean imitation) {
        final var periods = Quarter.values();
        final var numOfPeriods = periods.length;
        final var sumPerMonth = totalSum.divide(BigDecimal.valueOf(numOfPeriods), 2, RoundingMode.HALF_EVEN);
        final var sumLastMonth = totalSum.subtract(sumPerMonth.multiply(BigDecimal.valueOf(numOfPeriods - 1L)));

        for (final var month : periods) {
            final var sum = (month.getValue() == numOfPeriods) ? sumLastMonth : sumPerMonth;
            if (imitation) {
                sumMap.put(month, sum);
            } else {
                setLimitSharingPerPeriodSumAndBalance(limitSharingPerPeriodList.get(month), sum);
            }
        }
    }

    @Override
    public void distributeLimitSharingCurrentYear(Limit limit, BigDecimal totalSum, Map<? extends Quarter, LimitSharingPerPeriod> limitSharingPerPeriodListFull, Map<Quarter, BigDecimal> sumMap, boolean imitation) {
        final var currentMonth = Quarter.valueOf(LocalDate.now(ZoneOffset.UTC).getMonth().getValue());

        final var periods = Quarter.values();
        final int periodsSize = periods.length;
        final int numOfPeriods = periodsSize - currentMonth.ordinal();
        final var sumPerQuarter = totalSum.divide(BigDecimal.valueOf(numOfPeriods), 2, RoundingMode.HALF_EVEN);
        final var sumLastQuarter = totalSum.subtract(sumPerQuarter.multiply(BigDecimal.valueOf(numOfPeriods - 1L)));

        Map<? extends Quarter, LimitSharingPerPeriod> limitSharingPerPeriodList = null;
        if (!imitation) {
            limitSharingPerPeriodList = getSubList(limitSharingPerPeriodListFull, currentMonth, numOfPeriods,
                    periodsSize);
        }
        for (var i = 0; i < numOfPeriods; i++) {
            final var sum = (i == (numOfPeriods - 1)) ? sumLastQuarter : sumPerQuarter;
            if (imitation) {
                sumMap.put(periods[i + currentMonth.ordinal()], sum);
            } else {
                setLimitSharingPerPeriodSumAndBalance(limitSharingPerPeriodList.get(periods[i + currentMonth.ordinal()]), sum);
            }
        }
    }

    @Override
    public LimitSharingType type() {
        return LimitSharingType.QUARTER;
    }

    /**
     * Sets new sum and balance.
     *
     * @param limitSharingPerPeriod limit Sharing Per Period.
     * @param sum sum
     */
    private void setLimitSharingPerPeriodSumAndBalance(LimitSharingPerPeriod limitSharingPerPeriod, BigDecimal sum) {
        limitSharingPerPeriod.setSum(sum);
        limitSharingPerPeriod.setBalance(sum);
        limitSharingPerPeriodService.save(limitSharingPerPeriod);
    }

    private Map<? extends Quarter, LimitSharingPerPeriod> getSubList(
            Map<? extends Quarter, LimitSharingPerPeriod> limitSharingPerPeriodListFull,
            Quarter currentQuarter, int numOfPeriods, int totalPeriods
    ) {
        var quarters = limitSharingPerPeriodListFull.keySet().stream().sorted().skip(currentQuarter.ordinal()).limit(totalPeriods).toList();
        if (quarters.size() != numOfPeriods) {
            String sb = "distributeLimitSharingCurrentYear: getSubList: MONTHLY currentMonth: %s numOfPeriods: %d totalPeriods: %d"
                    .formatted(currentQuarter.name(), numOfPeriods, totalPeriods);
            throw new LimitLogicException(sb);
        }
        return limitSharingPerPeriodListFull.entrySet().stream().filter(entry -> quarters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
