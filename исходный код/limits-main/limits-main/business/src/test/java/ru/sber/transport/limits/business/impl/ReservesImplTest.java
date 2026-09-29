package ru.sber.transport.limits.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.business.Reserves;
import ru.sber.transport.limits.business.exceptions.ReserveNotFoundException;
import ru.sber.transport.limits.business.exceptions.ReserveNotSufficientException;
import ru.sber.transport.limits.business.exceptions.ServiceNotAvailableException;
import ru.sber.transport.limits.business.exceptions.TypeNotAvailableException;
import ru.sber.transport.limits.model.*;
import ru.sber.transport.limits.providers.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса резервирования")
class ReservesImplTest {

    private final Spendings spendings = mock(Spendings.class);

    private final Employees employees = mock(Employees.class);

    private final Services services = mock(Services.class);

    private final Types types = mock(Types.class);

    private final PeriodSharings periodSharings = mock(PeriodSharings.class);

    private final Reserves reserves = new ReservesImpl(spendings, employees, services, types, periodSharings);

    @Test
    @DisplayName("Проверка перерезервирования")
    void test_rereserve() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException {
        final var reserve = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::cost), BigDecimal.valueOf(100))
                .create();
        final var employee = Instancio.create(TestEmployee.class);
        final var service = Instancio.create(TestService.class);
        final var type = Instancio.create(TestType.class);
        final var periodSharing = Instancio.of(TestPeriodSharing.class)
                .set(Select.field(TestPeriodSharing::remains), BigDecimal.valueOf(50))
                .create();
        final var spending = Instancio.of(TestSpending.class)
                .set(Select.field(TestSpending::reserved), BigDecimal.valueOf(50))
                .create();

        when(employees.get(reserve.consumerId())).thenReturn(employee);
        when(periodSharings.get(service, type, reserve.date(), employee, false)).thenReturn(Optional.of(periodSharing));
        when(spendings.get(reserve.id())).thenReturn(Optional.of(spending));
        when(services.get(reserve.service())).thenReturn(Optional.of(service));
        when(types.get(reserve.type())).thenReturn(Optional.of(type));

        reserves.reserve(reserve);

        verify(periodSharings).update(periodSharing, periodSharing.remains().add(spending.reserved()).subtract(reserve.cost()));
        verify(spendings).create(periodSharing, reserve);
    }

    @Test
    @DisplayName("Проверка резервирования")
    void test_reserve() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException {
        final var reserve = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::cost), BigDecimal.valueOf(100))
                .create();
        final var employee = Instancio.create(TestEmployee.class);
        final var service = Instancio.create(TestService.class);
        final var type = Instancio.create(TestType.class);
        final var periodSharing = Instancio.of(TestPeriodSharing.class)
                .set(Select.field(TestPeriodSharing::remains), BigDecimal.valueOf(200))
                .create();

        when(employees.get(reserve.consumerId())).thenReturn(employee);
        when(periodSharings.get(service, type, reserve.date(), employee, false)).thenReturn(Optional.of(periodSharing));
        when(services.get(reserve.service())).thenReturn(Optional.of(service));
        when(types.get(reserve.type())).thenReturn(Optional.of(type));

        reserves.reserve(reserve);

        verify(periodSharings).update(periodSharing, periodSharing.remains().subtract(reserve.cost()));
        verify(spendings).create(periodSharing, reserve);
    }

    @Test
    @DisplayName("Проверка резервирования. Нет денег")
    void test_reserve_noMoney() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException {
        final var reserve = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::cost), BigDecimal.valueOf(200))
                .create();
        final var employee = Instancio.create(TestEmployee.class);
        final var service = Instancio.create(TestService.class);
        final var type = Instancio.create(TestType.class);
        final var periodSharing = Instancio.of(TestPeriodSharing.class)
                .set(Select.field(TestPeriodSharing::remains), BigDecimal.valueOf(100))
                .create();

        when(employees.get(reserve.consumerId())).thenReturn(employee);
        when(periodSharings.get(service, type, reserve.date(), employee, true)).thenReturn(Optional.of(periodSharing));
        when(services.get(reserve.service())).thenReturn(Optional.of(service));
        when(types.get(reserve.type())).thenReturn(Optional.of(type));

        try {
            reserves.reserve(reserve);
            fail("ReserveNotSufficientException expected");
        } catch (ReserveNotSufficientException e) {
            assertThat(e).isNotNull();
        }

        verify(periodSharings, never()).update(periodSharing, periodSharing.remains().subtract(reserve.cost()));
        verify(spendings, never()).create(periodSharing, reserve);
    }

    @Test
    @DisplayName("Проверка резервирования. Нет распределения")
    void test_reserve_noSharing() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException {
        final var reserve = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::cost), BigDecimal.valueOf(200))
                .create();
        final var employee = Instancio.create(TestEmployee.class);
        final var service = Instancio.create(TestService.class);
        final var type = Instancio.create(TestType.class);

        when(employees.get(reserve.consumerId())).thenReturn(employee);
        when(services.get(reserve.service())).thenReturn(Optional.of(service));
        when(types.get(reserve.type())).thenReturn(Optional.of(type));

        try {
            reserves.reserve(reserve);
            fail("ReserveNotSufficientException expected");
        } catch (ReserveNotSufficientException e) {
            assertThat(e).isNotNull();
        }

        verify(periodSharings, never()).update(any(), any());
        verify(spendings, never()).create(any(), eq(reserve));
    }

    @Test
    @DisplayName("Проверка резервирования. Нет типа")
    void test_reserve_noType() throws ServiceNotAvailableException, ReserveNotSufficientException {
        final var reserve = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::cost), BigDecimal.valueOf(200))
                .create();
        final var employee = Instancio.create(TestEmployee.class);
        final var service = Instancio.create(TestService.class);

        when(employees.get(reserve.consumerId())).thenReturn(employee);
        when(services.get(reserve.service())).thenReturn(Optional.of(service));

        try {
            reserves.reserve(reserve);
            fail("TypeNotAvailableException expected");
        } catch (TypeNotAvailableException e) {
            assertThat(e).isNotNull();
        }

        verify(periodSharings, never()).update(any(), any());
        verify(spendings, never()).create(any(), eq(reserve));
    }

    @Test
    @DisplayName("Проверка резервирования. Нет сервиса")
    void test_reserve_noService() throws ReserveNotSufficientException, TypeNotAvailableException {
        final var reserve = Instancio.of(TestReserve.class)
                .set(Select.field(TestReserve::cost), BigDecimal.valueOf(200))
                .create();
        final var employee = Instancio.create(TestEmployee.class);

        when(employees.get(reserve.consumerId())).thenReturn(employee);

        try {
            reserves.reserve(reserve);
            fail("ServiceNotAvailableException expected");
        } catch (ServiceNotAvailableException e) {
            assertThat(e).isNotNull();
        }

        verify(periodSharings, never()).update(any(), any());
        verify(spendings, never()).create(any(), eq(reserve));
    }

    @Test
    @DisplayName("Проверка отмены резервирования")
    void test_cancel() throws ReserveNotFoundException {
        final var id = UUID.randomUUID();
        final var periodSharing = Instancio.create(TestPeriodSharing.class);
        final var spending = Instancio.create(TestSpending.class);

        when(spendings.get(id)).thenReturn(Optional.of(spending));
        when(periodSharings.get(spending.periodSharing())).thenReturn(Optional.of(periodSharing));

        reserves.cancel(id);

        verify(periodSharings).update(periodSharing, periodSharing.remains().add(spending.reserved()));
        verify(spendings).update(spending, ReserveStatus.CANCELED);
    }

    @Test
    @DisplayName("Проверка отмены резервирования. Нет траты")
    void test_cancel_noSpending() {
        final var id = UUID.randomUUID();

        try {
            reserves.cancel(id);
        } catch (ReserveNotFoundException e) {
            assertThat(e).isNotNull();
        }

        verify(periodSharings, never()).update(any(), any());
        verify(spendings, never()).update(any(), any());
    }

    @Test
    @DisplayName("Проверка подтверждения резервирования")
    void test_configrm() throws ReserveNotFoundException {
        final var id = UUID.randomUUID();
        final var spending = Instancio.create(TestSpending.class);

        when(spendings.get(id)).thenReturn(Optional.of(spending));

        reserves.confirm(id);

        verify(spendings).update(spending, ReserveStatus.SPENT);
    }

    @Test
    @DisplayName("Проверка подтверждения резервирования. Нет траты")
    void test_confirm_noSpending() {
        final var id = UUID.randomUUID();

        try {
            reserves.confirm(id);
        } catch (ReserveNotFoundException e) {
            assertThat(e).isNotNull();
        }

        verify(spendings, never()).update(any(), any());
    }

    private record TestReserve(
            BigDecimal cost,
            UUID id,
            String service,
            String type,
            UUID consumerId,
            OffsetDateTime date
    ) implements Reserve {}

    private record TestEmployee(UUID id, UUID departmentId) implements Employee {}

    private record TestService(String name) implements Service {}

    private record TestType(String name) implements Type {}

    private record TestPeriodSharing(BigDecimal remains, UUID id) implements PeriodSharing {}

    private record TestSpending(BigDecimal reserved, UUID periodSharing, UUID id, ReserveStatus status) implements Spending {}

}