package ru.sber.transport.limits.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка бизнес-кейсов распределения лимитов по периодам")
class LimitSharingPerPeriodsImplTest {

    private final LimitSharingPerPeriodProvider provider = mock(LimitSharingPerPeriodProvider.class);

    private final LimitsProvider limits = mock(LimitsProvider.class);

    private final DepartmentsProvider departments = mock(DepartmentsProvider.class);

    private final EmployeeProvider employees = mock(EmployeeProvider.class);

    private final EmailSender sender = mock(EmailSender.class);

    private final LimitSharingPerPeriods limitSharingPerPeriods = new LimitSharingPerPeriodsImpl(provider, limits, departments, employees, sender);

    @Test
    @DisplayName("Распределение лимитов по периодам. Нет распределения")
    void test_check_no_limit_sharing() {
        limitSharingPerPeriods.checkRemains(UUID.randomUUID());

        verify(sender, never()).send(anySet(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Распределение лимитов по периодам. Нет общей суммы")
    void test_check_no_total() {
        var limitSharing = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getSum), BigDecimal.ZERO)
                .create();
        when(provider.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        limitSharingPerPeriods.checkRemains(limitSharing.getId());

        verify(sender, never()).send(anySet(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Распределение лимитов по периодам. Отрицательная сумма")
    void test_check_no_total_negative() {
        var limitSharing = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getSum), BigDecimal.ONE.negate())
                .create();
        when(provider.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        limitSharingPerPeriods.checkRemains(limitSharing.getId());

        verify(sender, never()).send(anySet(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Распределение лимитов по периодам. Осталось больше 30%")
    void test_check_more_30pc() {
        var limitSharing = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getSum), BigDecimal.valueOf(100))
                .set(Select.field(LimitSharingPerPeriod::getBalance), BigDecimal.valueOf(10))
                .create();
        when(provider.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        limitSharingPerPeriods.checkRemains(limitSharing.getId());

        verify(sender, never()).send(anySet(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Распределение лимитов по периодам. Осталось меньше 30%. Нет лимита")
    void test_check_less_30pc_no_limit() {
        var limitSharing = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getSum), BigDecimal.valueOf(100))
                .set(Select.field(LimitSharingPerPeriod::getBalance), BigDecimal.valueOf(80))
                .create();
        when(provider.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        limitSharingPerPeriods.checkRemains(limitSharing.getId());

        verify(sender, never()).send(anySet(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Распределение лимитов по периодам. Осталось меньше 30%. Нет подразделения")
    void test_check_less_30pc_no_department() {
        var limitSharing = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getSum), BigDecimal.valueOf(100))
                .set(Select.field(LimitSharingPerPeriod::getBalance), BigDecimal.valueOf(80))
                .create();

        var limit = Instancio.create(Limit.class);

        when(provider.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        limitSharingPerPeriods.checkRemains(limitSharing.getId());

        verify(sender, never()).send(anySet(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Распределение лимитов по периодам. Осталось меньше 30%")
    void test_check_less_30pc() {
        var limitSharing = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getSum), BigDecimal.valueOf(100))
                .set(Select.field(LimitSharingPerPeriod::getBalance), BigDecimal.valueOf(30))
                .ignore(Select.field(LimitSharingPerPeriod::getNoticedAt))
                .create();

        var limit = Instancio.create(Limit.class);
        var department = Instancio.create(Department.class);
        var emails = Instancio.createSet(String.class);

        when(limits.getOfSharing(limitSharing.getLimitSharingId())).thenReturn(Optional.of(limit));
        when(employees.getEmails(limit.getResponsibles())).thenReturn(emails);
        when(departments.get(limit.getDepartmentId())).thenReturn(Optional.of(department));
        when(provider.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));
        when(sender.send(emails, department.getHumanReadableId(), limit.getHumanReadableId(), 30)).thenReturn(List.of(CompletableFuture.completedFuture(null)));

        limitSharingPerPeriods.checkRemains(limitSharing.getId());

        verify(sender).send(emails, department.getHumanReadableId(), limit.getHumanReadableId(), 30);
        verify(provider).setNotified(limitSharing.getId());
    }
}