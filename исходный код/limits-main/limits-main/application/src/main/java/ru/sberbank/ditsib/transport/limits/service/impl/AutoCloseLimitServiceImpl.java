package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.RemainsTransferTarget;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.AutoCloseLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSettingsService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AutoCloseLimitServiceImpl implements AutoCloseLimitService {
    
    private final LimitService limitService;
    
    private final LimitSharingService limitSharingService;
    
    private final LimitSettingsService limitSettingsService;

    private final ObjectProvider<AutoCloseLimitService> autoCloseLimitService;
    
    private static final Integer THREADNUM = 5;
    
    private ScheduledFuture<?> scheduledFuture;
    
    private class RunnableTask implements Runnable {
        @Override
        public void run() {
            Calendar cal = Calendar.getInstance();
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayOfMonth == lastDayOfMonth) {
                autoCloseLimitService.getObject().returnRemains(null);
            }
        }
    }
    
    /**
     * Init data pool.
     */
    @PostConstruct
    public void postConstruct() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(THREADNUM);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        threadPoolTaskScheduler.initialize();
        
        String cronExpression = "0 30 23 * * *";
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = threadPoolTaskScheduler.schedule(new RunnableTask(),
                                                           new org.springframework.scheduling.support.CronTrigger(cronExpression));
    }
    
    @Override
    @Transactional
    public void returnRemains(Period periodNumber) {
        doProcessRemains(periodNumber);
    }
    
    private void doProcessRemains(Period periodNumber) {
        var now = LocalDate.now(ZoneOffset.UTC);
        List<Limit> limitList = new ArrayList<>();
        if (now.getMonth().ordinal() % 3 == 0) {
            limitList.addAll(limitService.getByLimitSharingType(LimitSharingType.QUARTER));
        }
        // get monthly limits
        limitList.addAll(limitService.getByLimitSharingType(LimitSharingType.MONTHLY));
        limitList.addAll(limitService.getByLimitSharingType(LimitSharingType.PERCENTS));
        for (Limit limit : limitList) {
            DepLimit mainLimit = getUpperParent(limit);
            if (!limit.getId().equals(mainLimit.getId())) {
                String transferTarget;
                if (limit instanceof DepLimit) {
                    transferTarget = limitSettingsService.getByName(SettingsNames.DEP_LIMIT_REMAINS_TARGET);
                } else {
                    transferTarget = limitSettingsService.getByName(SettingsNames.EMP_LIMIT_REMAINS_TARGET);
                }
                if (periodNumber == null) {
                    periodNumber = getCurrentPeriodNumber(limit);
                }
                var target = RemainsTransferTarget.valueOf(transferTarget);
                List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limit);
                for (var limitSharing : limitSharingList) {
                    if (target == RemainsTransferTarget.TO_RESERVE) {
                        limitSharingService.moveRemainsToEconomy(limitSharing, ReflectionUtils.cast(periodNumber));
                    }
                    if (target == RemainsTransferTarget.TO_NEXT_PERIOD) {
                        int maxPeriod = limitSharing.getLimit().getLimitSharingType() == LimitSharingType.QUARTER ? 4 : 12;
                        if (periodNumber.getValue() > maxPeriod) {
                            return;
                        }
                        limitSharingService.moveRemainsToNextPeriod(limitSharing, ReflectionUtils.cast(periodNumber));
                    }
                }
            }
        }
    }
    
    private Period getCurrentPeriodNumber(Limit limit) {
        if ((limit.getLimitSharingType() == LimitSharingType.PERCENTS)
            || (limit.getLimitSharingType() == LimitSharingType.MONTHLY)) {
            return Month.valueOf(LocalDate.now(ZoneOffset.UTC).getMonth());
        } else if (limit.getLimitSharingType() == LimitSharingType.QUARTER) {
            return Quarter.valueOf(Calendar.getInstance().get(Calendar.MONTH) / 3);
        } else {
            throw new UnsupportedOperationException("Unsupported sharing type %s".formatted(limit.getLimitSharingType()));
        }
    }
    
    
    private DepLimit getUpperParent(Limit limit) {
        var parentLimit = limit;
        while (parentLimit.getParent() != null) {
            parentLimit = parentLimit.getParent();
        }
        return (DepLimit) parentLimit;
    }
}
