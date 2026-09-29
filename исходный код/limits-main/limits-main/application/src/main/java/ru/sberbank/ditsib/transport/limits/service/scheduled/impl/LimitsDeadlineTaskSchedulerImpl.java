package ru.sberbank.ditsib.transport.limits.service.scheduled.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.dao.DeadlineSettingsRepository;
import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;
import ru.sberbank.ditsib.transport.limits.service.scheduled.LimitsDeadlineTaskScheduler;

import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus.INIT;
import static ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus.LimitStatusCode.LIMIT_DECLINED_BY_EXPIRATION_TIME;

@Component
@RequiredArgsConstructor
@Slf4j
public class LimitsDeadlineTaskSchedulerImpl implements LimitsDeadlineTaskScheduler {
    
    private final DeadlineSettingsRepository settingsRepository;
    private final LimitRequestService requestService;
    
    @Override
    @Transactional
    public void checkRequestDeadlines() {
        for (DeadlineSettings settings : settingsRepository.findAll()) {
            cancelLimitRequestsAfterDeadline(settings);
        }
    }
    
    private void cancelLimitRequestsAfterDeadline(DeadlineSettings settings) {
        List<LimitRequest> requests =
                requestService.getByOrganizationIdAndStatus(settings.getOrganizationId(), INIT);
        for (LimitRequest request : requests) {
            if (isExpired(request, settings)) {
                requestService.cancel(request, LIMIT_DECLINED_BY_EXPIRATION_TIME);
            }
        }
    }
    
    private boolean isExpired(LimitRequest request, DeadlineSettings settings) {
        return Duration.between(request.getCreationTime(), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .compareTo(settings.getDuration(request.getLimitType())) >= 0;
    }
}
