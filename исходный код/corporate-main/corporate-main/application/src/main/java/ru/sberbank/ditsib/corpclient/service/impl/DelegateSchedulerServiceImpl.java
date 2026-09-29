package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.service.DelegateService;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * Реализация сервиса сотрудников.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class DelegateSchedulerServiceImpl {

    private final DelegateService delegateService;

    private static final Integer THREADNUM = 3;
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Value("${config.delayDelegateDeadlineDisactivationHours:2}") private int delayDelegateDeadlineDisactivationHours;

    @PostConstruct
    private void configure() {
        Duration duration = Duration.ofHours(delayDelegateDeadlineDisactivationHours);
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(THREADNUM);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.scheduleAtFixedRate(new RunnableTask(), duration);
    }

    @NoArgsConstructor
    private class RunnableTask implements Runnable {

        @Override
        @Transactional
        public void run() {
            delegateService.updateDelegateStatusByDeadline();
        }
    }

}
