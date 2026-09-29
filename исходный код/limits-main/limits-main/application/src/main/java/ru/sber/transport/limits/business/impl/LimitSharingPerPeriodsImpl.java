package ru.sber.transport.limits.business.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.limits.business.LimitSharingPerPeriods;
import ru.sber.transport.limits.business.model.Department;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;
import ru.sber.transport.limits.business.providers.DepartmentsProvider;
import ru.sber.transport.limits.business.providers.EmployeeProvider;
import ru.sber.transport.limits.business.providers.LimitSharingPerPeriodProvider;
import ru.sber.transport.limits.business.providers.LimitsProvider;
import ru.sber.transport.limits.messaging.senders.EmailSender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LimitSharingPerPeriodsImpl implements LimitSharingPerPeriods {

    private final LimitSharingPerPeriodProvider limitSharingPerPeriods;

    private final LimitsProvider limits;

    private final DepartmentsProvider departments;

    private final EmployeeProvider employees;

    private final EmailSender emailSender;

    @Override
    public void checkRemains(@NonNull UUID id) {
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> sendEmailIfNecessary(id));
        }
    }

    private void sendEmailIfNecessary(@NotNull UUID id) {
        limitSharingPerPeriods.get(id).ifPresent(limitSharingPerPeriod -> {
            if (limitSharingPerPeriod.getNoticedAt() != null) {
                return;
            }
            final var remains = limitSharingPerPeriod.getBalance();
            final var total = limitSharingPerPeriod.getSum();
            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            final var percent = remains.divide(total, 2, RoundingMode.DOWN).doubleValue();
            if (percent <= .3) {
                limits.getOfSharing(limitSharingPerPeriod.getLimitSharingId())
                        .ifPresent(limit -> {
                            log.info("It is remains less than 30% for limit {} at month {}", limit.getHumanReadableId(), limitSharingPerPeriod.getPeriod());
                            departments.get(limit.getDepartmentId()).ifPresent(department -> sendEmail(limit, limitSharingPerPeriod, department));
                        });
            }
        });
    }

    private void sendEmail(Limit limit, LimitSharingPerPeriod limitSharingPerPeriod, Department department) {
        final var responsibles = limit.getResponsibles();
        if (responsibles.isEmpty()) {
            log.info("Responsibles are empty for limit {}", limit.getHumanReadableId());
        }
        final var emails = employees.getEmails(responsibles);
        if (!emails.isEmpty()) {
            log.info("Sending email notifications for limit {}", limit.getHumanReadableId());
            final var futures = emailSender.send(emails, department.getHumanReadableId(), limit.getHumanReadableId(), 30);
            if (futures.isEmpty()) {
                log.info("Email notifications are not sent for limit {}", limit.getHumanReadableId());
            } else {
                log.info("Email notifications for limit {} sent", limit.getHumanReadableId());
                limitSharingPerPeriods.setNotified(limitSharingPerPeriod.getId());
            }
        } else {
            log.info("Responsible emails are empty for limit {}", limit.getHumanReadableId());
        }
    }
}
