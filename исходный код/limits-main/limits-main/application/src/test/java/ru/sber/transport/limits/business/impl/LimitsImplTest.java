package ru.sber.transport.limits.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.dto.Page;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.limits.business.Limits;
import ru.sber.transport.limits.business.exceptions.ClosingUpperLevelNotAllowedException;
import ru.sber.transport.limits.business.exceptions.UpdateNotAllowedException;
import ru.sber.transport.limits.business.model.Period;
import ru.sber.transport.limits.business.model.*;
import ru.sber.transport.limits.business.providers.*;
import ru.sber.transport.limits.web.http.model.LimitWebFilter;
import ru.sberbank.ditsib.request.Direction;

import java.math.BigDecimal;
import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка бизнес-кейсов лимитов")
class LimitsImplTest {

    private final LimitsProvider provider = mock(LimitsProvider.class);

    private final LimitSharingProvider limitSharingProvider = mock(LimitSharingProvider.class);

    private final EmployeeProvider employees = mock(EmployeeProvider.class);

    private final LimitSharingPerPeriodProvider limitsPerPeriodProvider = mock(LimitSharingPerPeriodProvider.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final DepartmentsProvider departments = mock(DepartmentsProvider.class);

    private final Limits limits = new LimitsImpl(provider, limitSharingProvider, limitsPerPeriodProvider, employees, Clock.systemUTC(), "0 0 1 * * *", employeeOrganizationFunction, departments);

    @Test
    @DisplayName("Проверка удаления лимита. Не найден")
    void test_delete_not_found() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        assertThatThrownBy(() -> limits.delete(limitId, userId, false))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityId", limitId)
                .hasFieldOrPropertyWithValue("entityName", "Limit")
        ;
    }

    @Test
    @DisplayName("Проверка удаления лимита. Нет пользователя")
    void test_delete_no_user() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limitId)
                .set(Select.field(Limit::getStatus), Status.PLANNING)
                .ignore(Select.field(Limit::getParentId))
                .create();

        when(provider.get(limitId)).thenReturn(Optional.of(limit));

        assertThatThrownBy(() -> limits.delete(limitId, userId, false))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Trying to unauthorized access!!! Employee: %s".formatted(userId));
    }

    @Test
    @DisplayName("Проверка удаления лимита. Несовпадение с организацией")
    void test_delete_organization_mismatch() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limitId)
                .set(Select.field(Limit::getStatus), Status.PLANNING)
                .ignore(Select.field(Limit::getParentId))
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .create();

        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(employees.get(userId)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> limits.delete(limitId, userId, false))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Trying to unauthorized access!!! Employee: %s".formatted(userId));
    }

    @Test
    @DisplayName("Проверка удаления лимита. СМД")
    void test_delete_smd() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limitId)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .create();
        var parent = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limit.getParentId())
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .ignore(Select.field(Limit::getParentId))
                .create();

        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(employees.get(userId)).thenReturn(Optional.of(employee));
        when(provider.upper(limitId)).thenReturn(Optional.of(parent));
        when(provider.getChildren(limitId)).thenReturn(List.of(limit));

        limits.delete(limitId, userId, true);

        var limitCaptor = ArgumentCaptor.forClass(Limit.class);

        verify(provider, times(2)).save(limitCaptor.capture());

        var datas = limitCaptor.getAllValues();
        var actualLimit = datas.get(0);
        var actualParent = datas.get(1);
        assertSoftly(it -> {
            it.assertThat(actualLimit.getId()).isEqualTo(limitId);
            it.assertThat(actualLimit.getParentId()).isEqualTo(parent.getId());
            it.assertThat(actualLimit.getStatus()).isEqualTo(Status.CLOSED);
            it.assertThat(actualLimit.getSum()).isZero();
            it.assertThat(actualLimit.getReserve()).isZero();
        });
        assertSoftly(it -> {
            it.assertThat(actualParent.getId()).isEqualTo(parent.getId());
            it.assertThat(actualParent.getStatus()).isEqualTo(parent.getStatus());
            it.assertThat(actualParent.getSum()).isEqualTo(parent.getSum().add(limit.getSum()));
            it.assertThat(actualParent.getReserve()).isEqualTo(parent.getReserve().add(limit.getReserve()));
        });
    }

    @Test
    @DisplayName("Проверка удаления лимита. Нет родителя")
    void test_delete_no_parent() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limitId)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .ignore(Select.field(Limit::getParentId))
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(employees.get(userId)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> limits.delete(limitId, userId, false))
                .isInstanceOf(ClosingUpperLevelNotAllowedException.class)
                .hasFieldOrPropertyWithValue("limitId", limitId)
        ;
    }

    @Test
    @DisplayName("Проверка удаления лимита")
    void test_delete() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limitId)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .set(Select.field(Limit::getHash), "0")
                .create();
        var limitChild = Instancio.of(Limit.class)
                .set(Select.field(Limit::getParentId), limitId)
                .set(Select.field(Limit::getHash), "2")
                .create();
        var parent = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limit.getParentId())
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .set(Select.field(Limit::getHash), "3")
                .ignore(Select.field(Limit::getParentId))
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.getChildren(limitId)).thenReturn(List.of(limit, limitChild));
        when(provider.upper(limitId)).thenReturn(Optional.of(parent));
        when(employees.get(userId)).thenReturn(Optional.of(employee));

        limits.delete(limitId, userId, false);

        var limitCaptor = ArgumentCaptor.forClass(Limit.class);

        verify(provider, times(3)).save(limitCaptor.capture());

        var datas = limitCaptor.getAllValues();
        datas.sort(Comparator.comparing(Limit::getHash));
        var actualLimit = datas.get(0);
        var actualLimitChild = datas.get(1);
        var actualParent = datas.get(2);
        assertSoftly(it -> {
            it.assertThat(actualLimit.getId()).isEqualTo(limitId);
            it.assertThat(actualLimit.getParentId()).isEqualTo(parent.getId());
            it.assertThat(actualLimit.getStatus()).isEqualTo(Status.CLOSED);
            it.assertThat(actualLimit.getSum()).isZero();
            it.assertThat(actualLimit.getReserve()).isZero();
        });
        assertSoftly(it -> {
            it.assertThat(actualLimitChild.getId()).isEqualTo(limitChild.getId());
            it.assertThat(actualLimitChild.getParentId()).isEqualTo(limit.getId());
            it.assertThat(actualLimitChild.getStatus()).isEqualTo(Status.CLOSED);
            it.assertThat(actualLimitChild.getSum()).isZero();
            it.assertThat(actualLimitChild.getReserve()).isZero();
        });
        assertSoftly(it -> {
            it.assertThat(actualParent.getId()).isEqualTo(parent.getId());
            it.assertThat(actualParent.getParentId()).isNull();
            it.assertThat(actualParent.getStatus()).isEqualTo(Status.SHARED);
            it.assertThat(actualParent.getSum()).isEqualTo(parent.getSum().add(limit.getSum()));
            it.assertThat(actualParent.getReserve()).isEqualTo(parent.getReserve().add(limit.getReserve()));
        });
    }

    @Test
    @DisplayName("Проверка удаления лимита с распределениями")
    void test_delete_with_sharings() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limitId)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .create();
        var limitChild = Instancio.of(Limit.class)
                .set(Select.field(Limit::getParentId), limitId)
                .create();
        var parent = Instancio.of(Limit.class)
                .set(Select.field(Limit::getId), limit.getParentId())
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .ignore(Select.field(Limit::getParentId))
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();
        var limitSharings = Instancio.create(LimitSharing.class);
        var limitSharingPerPeriod = Instancio.create(LimitSharingPerPeriod.class);

        var limitSharingSum = limitSharings.getSum();
        var limitSharingBalance = limitSharings.getRemains();
        var limitSharingPerPeriodSum = limitSharingPerPeriod.getSum();
        var limitSharingPerPeriodBalance = limitSharingPerPeriod.getBalance();

        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.getChildren(limitId)).thenReturn(List.of(limit, limitChild));
        when(provider.upper(limitId)).thenReturn(Optional.of(parent));
        when(employees.get(userId)).thenReturn(Optional.of(employee));
        when(limitSharingProvider.getAll(limitId)).thenReturn(List.of(limitSharings));
        when(limitsPerPeriodProvider.getAll(limitSharings.getId())).thenReturn(List.of(limitSharingPerPeriod));

        limits.delete(limitId, userId, false);

        var limitCaptor = ArgumentCaptor.forClass(Limit.class);
        var limitSharingsCaptor = ArgumentCaptor.forClass(LimitSharing.class);
        var limitSharingPerPeriodCaptor = ArgumentCaptor.forClass(LimitSharingPerPeriod.class);

        verify(provider, times(3)).save(limitCaptor.capture());
        verify(limitSharingProvider, times(1)).save(limitSharingsCaptor.capture());
        verify(limitsPerPeriodProvider, times(1)).save(limitSharingPerPeriodCaptor.capture());

        var datas = limitCaptor.getAllValues();
        var actualLimit = datas.get(1);
        var actualLimitChild = datas.get(0);
        var actualParent = datas.get(2);
        assertSoftly(it -> {
            it.assertThat(actualLimit.getId()).isEqualTo(limitId);
            it.assertThat(actualLimit.getParentId()).isEqualTo(parent.getId());
            it.assertThat(actualLimit.getStatus()).isEqualTo(Status.CLOSED);
            it.assertThat(actualLimit.getSum()).isZero();
            it.assertThat(actualLimit.getReserve()).isZero();
        });
        assertSoftly(it -> {
            it.assertThat(actualLimitChild.getId()).isEqualTo(limitChild.getId());
            it.assertThat(actualLimitChild.getParentId()).isEqualTo(limit.getId());
            it.assertThat(actualLimitChild.getStatus()).isEqualTo(Status.CLOSED);
            it.assertThat(actualLimitChild.getSum()).isZero();
            it.assertThat(actualLimitChild.getReserve()).isZero();
        });
        assertSoftly(it -> {
            it.assertThat(actualParent.getId()).isEqualTo(parent.getId());
            it.assertThat(actualParent.getParentId()).isNull();
            it.assertThat(actualParent.getStatus()).isEqualTo(Status.SHARED);
            it.assertThat(actualParent.getSum()).isEqualTo(parent.getSum().add(limit.getSum()));
            it.assertThat(actualParent.getReserve()).isEqualTo(parent.getReserve().add(limit.getReserve()));
        });
        assertThat(limitSharingsCaptor.getValue().getRemains()).isZero();
        assertThat(limitSharingsCaptor.getValue().getSum()).isEqualTo(limitSharingSum.subtract(limitSharingBalance));
        assertThat(limitSharingPerPeriodCaptor.getValue().getBalance()).isZero();
        assertThat(limitSharingPerPeriodCaptor.getValue().getSum()).isEqualTo(limitSharingPerPeriodSum.subtract(limitSharingPerPeriodBalance));
    }

    @Test
    @DisplayName("Проверка обработки лимитов. Январь")
    void test_processLimits_january() {
        var utc = ZoneOffset.UTC;
        var clock = Clock.fixed(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0).toInstant(utc), ZoneId.of(utc.getId()));

        ((LimitsImpl) limits).processLimits(clock);

        verify(limitsPerPeriodProvider, never()).get(any(UUID.class));
    }

    @Test
    @DisplayName("Проверка обработки лимитов")
    void test_processLimits() {
        var utc = ZoneOffset.UTC;
        var next = LocalDate.of(2024, Month.APRIL, 1);
        var previous = next.minusMonths(1);
        var clock = Clock.fixed(next.atStartOfDay().toInstant(utc), ZoneId.of(utc.getId()));
        var sharingId = UUID.randomUUID();
        var previousLimit = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getLimitSharingId), sharingId)
                .set(Select.field(LimitSharingPerPeriod::getPeriod), Period.MARCH)
                .set(Select.field(LimitSharingPerPeriod::isMovedToNext), false)
                .create();
        var nextLimit = Instancio.of(LimitSharingPerPeriod.class)
                .set(Select.field(LimitSharingPerPeriod::getLimitSharingId), sharingId)
                .set(Select.field(LimitSharingPerPeriod::getPeriod), Period.APRIL)
                .create();
        var previousBalance = previousLimit.getBalance();
        var previousSum = previousLimit.getSum();
        var nextBalance = nextLimit.getBalance();
        var nextSum = nextLimit.getSum();

        when(limitsPerPeriodProvider.getNotMoved(previous)).thenReturn(List.of(previousLimit));
        when(limitsPerPeriodProvider.get(next)).thenReturn(List.of(nextLimit));

        ((LimitsImpl) limits).processLimits(clock);

        var limitsCaptor = ArgumentCaptor.forClass(LimitSharingPerPeriod.class);

        verify(limitsPerPeriodProvider, times(2)).save(limitsCaptor.capture());

        var values = limitsCaptor.getAllValues();
        assertThat(values).hasSize(2);

        var actualNext = values.get(0);
        var actualPrevious = values.get(1);

        assertSoftly(it -> {
            it.assertThat(actualNext.getId()).isEqualTo(nextLimit.getId());
            it.assertThat(actualNext.getSum()).isEqualTo(nextSum.add(previousBalance));
            it.assertThat(actualNext.getBalance()).isEqualTo(nextBalance.add(previousBalance));
        });

        assertSoftly(it -> {
            it.assertThat(actualPrevious.getId()).isEqualTo(previousLimit.getId());
            it.assertThat(actualPrevious.getSum()).isEqualTo(previousBalance);
            it.assertThat(actualPrevious.getBalance()).isZero();
        });
    }

    @Test
    @DisplayName("Проверка получения лимита. Нет лимита")
    void test_get_no_limit() {
        var limitId = UUID.randomUUID();
        try {
            limits.get(UUID.randomUUID(), false, limitId);
            fail("EntityNotFoundException is expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityId", limitId)
                    .hasFieldOrPropertyWithValue("entityName", "Limit")
            ;
        }
    }

    @Test
    @DisplayName("Проверка получения хэша. Нет лимита")
    void test_hash_no_limit() {
        var limitId = UUID.randomUUID();
        try {
            limits.hash(UUID.randomUUID(), false, limitId);
            fail("EntityNotFoundException is expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityId", limitId)
                    .hasFieldOrPropertyWithValue("entityName", "Limit")
            ;
        }
    }

    @Test
    @DisplayName("Проверка получения модифицированного лимита. Нет лимита")
    void test_get_modified_no_limit() {
        var limitId = UUID.randomUUID();
        try {
            limits.get(UUID.randomUUID(), false, limitId, OffsetDateTime.now());
            fail("EntityNotFoundException is expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityId", limitId)
                    .hasFieldOrPropertyWithValue("entityName", "Limit")
            ;
        }
    }

    @Test
    @DisplayName("Проверка получения модифицированного хэша. Нет лимита")
    void test_hash_modified_no_limit() {
        var limitId = UUID.randomUUID();
        try {
            limits.hash(UUID.randomUUID(), false, limitId, OffsetDateTime.now());
            fail("EntityNotFoundException is expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityId", limitId)
                    .hasFieldOrPropertyWithValue("entityName", "Limit")
            ;
        }
    }

    @Test
    @DisplayName("Проверка получения лимита. Не разрешено")
    void test_get_no_limit_not_allowed() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(Limit.class);

        when(employeeOrganizationFunction.apply(userId)).thenReturn(UUID.randomUUID());
        when(provider.get(limitId)).thenReturn(Optional.of(data));
        assertThatThrownBy(() -> limits.get(userId, false, limitId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Trying to unauthorized access!!! Employee: %s".formatted(userId))
        ;
    }

    @Test
    @DisplayName("Проверка получения хэша. Не разрешено")
    void test_hash_no_limit_not_allowed() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(Limit.class);

        when(employeeOrganizationFunction.apply(userId)).thenReturn(UUID.randomUUID());
        when(provider.hash(limitId)).thenReturn(Optional.of(data));
        assertThatThrownBy(() -> limits.hash(userId, false, limitId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Trying to unauthorized access!!! Employee: %s".formatted(userId))
        ;
    }

    @Test
    @DisplayName("Проверка получения модифицированного лимита. Не разрешено")
    void test_get_modified_no_limit_not_allowed() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(ModifiedLimit.class);

        var now = OffsetDateTime.now();

        when(employeeOrganizationFunction.apply(userId)).thenReturn(UUID.randomUUID());
        when(provider.get(limitId, now)).thenReturn(Optional.of(data));
        assertThatThrownBy(() -> limits.get(userId, false, limitId, now))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Trying to unauthorized access!!! Employee: %s".formatted(userId))
        ;
    }

    @Test
    @DisplayName("Проверка получения модифицированного хэша. Не разрешено")
    void test_hash_modified_no_limit_not_allowed() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(ModifiedLimit.class);

        var now = OffsetDateTime.now();

        when(employeeOrganizationFunction.apply(userId)).thenReturn(UUID.randomUUID());
        when(provider.get(limitId, now)).thenReturn(Optional.of(data));
        assertThatThrownBy(() -> limits.get(userId, false, limitId, now))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Trying to unauthorized access!!! Employee: %s".formatted(userId))
        ;
    }

    @Test
    @DisplayName("Проверка получения лимита. Не разрешено")
    void test_get() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(Limit.class);

        when(employeeOrganizationFunction.apply(userId)).thenReturn(data.getOrganizationId());
        when(provider.get(limitId)).thenReturn(Optional.of(data));

        var limit = limits.get(userId, false, limitId);

        assertThat(limit).isEqualTo(data);
    }

    @Test
    @DisplayName("Проверка получения хэша")
    void test_hash() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(Limit.class);

        when(provider.hash(limitId)).thenReturn(Optional.of(data));

        var limit = limits.hash(userId, true, limitId);

        assertThat(limit).isEqualTo(data);
    }

    @Test
    @DisplayName("Проверка получения модифицированного лимита")
    void test_get_modified() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(ModifiedLimit.class);

        var now = OffsetDateTime.now();

        when(employeeOrganizationFunction.apply(userId)).thenReturn(data.data().getOrganizationId());
        when(provider.get(limitId, now)).thenReturn(Optional.of(data));

        var limit = limits.get(userId, false, limitId, now);

        assertThat(limit).isEqualTo(data);
    }

    @Test
    @DisplayName("Проверка получения модифицированного хэша")
    void test_hash_modified() {
        var limitId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var data = Instancio.create(ModifiedLimit.class);

        var now = OffsetDateTime.now();

        when(provider.hash(limitId, now)).thenReturn(Optional.of(data));

        var limit = limits.hash(userId, true, limitId, now);

        assertThat(limit).isEqualTo(data);
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. Нет лимита")
    void test_update_no_limit() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.create(Limit.class);
        final var fields = Instancio.createList(String.class);

        assertThatThrownBy(() -> limits.update(limitId, userId, forceAllow, newData, fields))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityId", limitId)
                .hasFieldOrPropertyWithValue("entityName", "Limit");
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. Нет доступа")
    void test_update_no_access() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.create(Limit.class);
        final var fields = Instancio.createList(String.class);
        final var limit = Instancio.create(Limit.class);

        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(employeeOrganizationFunction.apply(userId)).thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> limits.update(limitId, userId, forceAllow, newData, fields))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(String.format("Trying to unauthorized access!!! Employee: %s", userId));
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. СМД")
    void test_update_smd() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = true;
        final var newData = Instancio.create(Limit.class);
        final var fields = Instancio.createList(String.class);
        final var limit = Instancio.create(Limit.class);
        final var value = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(employees.get(any())).thenReturn(Optional.of(value));
        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.update(limit, newData, fields)).thenReturn(limit);

        var actual = limits.update(limitId, userId, forceAllow, newData, fields);

        assertThat(actual).isEqualTo(limit);
    }

    @Test
    @DisplayName("Проверка обновления данных лимита")
    void test_update() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.create(Limit.class);
        final var fields = Instancio.createList(String.class);
        final var limit = Instancio.create(Limit.class);
        final var value = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(employees.get(any())).thenReturn(Optional.of(value));
        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.update(limit, newData, fields)).thenReturn(limit);
        when(employeeOrganizationFunction.apply(userId)).thenReturn(limit.getOrganizationId());

        var actual = limits.update(limitId, userId, forceAllow, newData, fields);

        assertThat(actual).isEqualTo(limit);
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. Изменена сумма. Распределено")
    void test_update_sumReplaced_shared() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.of(Limit.class)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(50))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(500))
                .create();
        final var fields = List.of("REPLACE:sum", "REPLACE:reserve");
        final var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(10))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(100))
                .create();
        final var value = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(employees.get(any())).thenReturn(Optional.of(value));
        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.update(limit, newData, fields)).thenReturn(limit);
        when(employeeOrganizationFunction.apply(userId)).thenReturn(limit.getOrganizationId());

        try {
            limits.update(limitId, userId, forceAllow, newData, fields);
            fail("Should throw UpdateNotAllowedException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UpdateNotAllowedException.class);
        }
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. Добавлена сумма. Распределено")
    void test_update_sumAdded_shared() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.of(Limit.class)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(10))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(100))
                .create();
        final var fields = List.of("ADD:sum", "ADD:reserve");
        final var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(10))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(100))
                .create();
        final var value = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(employees.get(any())).thenReturn(Optional.of(value));
        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.update(limit, newData, fields)).thenReturn(limit);
        when(employeeOrganizationFunction.apply(userId)).thenReturn(limit.getOrganizationId());

        try {
            limits.update(limitId, userId, forceAllow, newData, fields);
            fail("Should throw UpdateNotAllowedException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UpdateNotAllowedException.class);
        }
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. Сумма не изменена. Распределено")
    void test_update_sumNotReplaced_shared() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.of(Limit.class)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(10))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(100))
                .create();
        final var fields = List.of("REPLACE:sum", "REPLACE:reserve");
        final var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(10))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(100))
                .create();
        final var value = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(employees.get(any())).thenReturn(Optional.of(value));
        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.update(limit, newData, fields)).thenReturn(limit);
        when(employeeOrganizationFunction.apply(userId)).thenReturn(limit.getOrganizationId());


        var actual = limits.update(limitId, userId, forceAllow, newData, fields);

        assertThat(actual).isEqualTo(limit);
    }

    @Test
    @DisplayName("Проверка обновления данных лимита. Не добавлена сумма. Распределено")
    void test_update_sumNotAdded_shared() {
        final var limitId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var forceAllow = false;
        final var newData = Instancio.of(Limit.class)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(0))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(0))
                .create();
        final var fields = List.of("ADD:sum", "ADD:reserve");
        final var limit = Instancio.of(Limit.class)
                .set(Select.field(Limit::getStatus), Status.SHARED)
                .set(Select.field(Limit::getSum), BigDecimal.valueOf(10))
                .set(Select.field(Limit::getReserve), BigDecimal.valueOf(100))
                .create();
        final var value = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), limit.getOrganizationId())
                .create();

        when(employees.get(any())).thenReturn(Optional.of(value));
        when(provider.get(limitId)).thenReturn(Optional.of(limit));
        when(provider.update(limit, newData, fields)).thenReturn(limit);
        when(employeeOrganizationFunction.apply(userId)).thenReturn(limit.getOrganizationId());

        var actual = limits.update(limitId, userId, forceAllow, newData, fields);

        assertThat(actual).isEqualTo(limit);
    }

    @Test
    @DisplayName("Проверка получения списка лимитов. Не авторизован")
    void test_getAll_unauthorized() {
        var employee = Instancio.create(Employee.class);
        when(employees.current()).thenReturn(employee);
        when(departments.get(any())).thenReturn(Optional.of(Instancio.create(Department.class)));
        try {
            limits.get(LimitWebFilter.builder().departmentId(UUID.randomUUID()).build(), Instancio.create(Integer.class), Instancio.create(Integer.class), Instancio.create(String.class), Instancio.create(Direction.class));
            fail("Should throw UnauthorizedException");
        } catch (UnauthorizedException e) {
            assertThat(e.getMessage()).isEqualTo("Trying to unauthorized access!!! Employee: " + employee.getId());
        }
    }

    @Test
    @DisplayName("Проверка получения списка лимитов. Подразделение не найдено")
    void test_getAll_departmentNotFound() {
        var departmentId = UUID.randomUUID();
        when(employees.current()).thenReturn(Instancio.create(Employee.class));
        try {
            limits.get(LimitWebFilter.builder().departmentId(departmentId).build(), Instancio.create(Integer.class), Instancio.create(Integer.class), Instancio.create(String.class), Instancio.create(Direction.class));
        } catch (EntityNotFoundException e) {
            assertThat(e.getEntityName()).isEqualTo("Department");
            assertThat(e.getEntityId()).isEqualTo(departmentId);
        }
    }

    @Test
    @DisplayName("Проверка получения списка лимитов. СМД. Подразделение не указано")
    void test_getAll_dm_departmentNotDefined() {
        var employee = Instancio.create(Employee.class);
        var direction = Instancio.create(Direction.class);
        var sort = Instancio.create(String.class);
        var size = Instancio.create(Integer.class);
        var page = Instancio.create(Integer.class);
        var serviceType = Instancio.create(String.class);
        var status = Instancio.create(Status.class);
        var year = Instancio.create(Integer.class);
        var pageData = new Page<>(new PageImpl<>(Instancio.createList(Limit.class), PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort), 100));

        when(employees.current()).thenReturn(employee);
        when(employees.hasAccess()).thenReturn(true);
        when(employeeOrganizationFunction.apply(employee.getId())).thenReturn(employee.getOrganizationId());
        var filter = LimitWebFilter.builder().organizationId(employee.getOrganizationId()).year(year).status(status).serviceType(serviceType).build();
        when(provider.get(employee.getOrganizationId(), filter, page, size, sort, direction))
                .thenReturn(pageData);

        var actual = limits.get(filter, page, size, sort, direction);

        assertThat(actual.getContent()).hasSameElementsAs(pageData.getContent());
        assertThat(actual.getPageData().size()).isEqualTo(pageData.getPageData().size());
        assertThat(actual.getPageData().last()).isEqualTo(pageData.getPageData().last());
        assertThat(actual.getPageData().first()).isEqualTo(pageData.getPageData().first());
        assertThat(actual.getPageData().totalPages()).isEqualTo(pageData.getPageData().totalPages());
        assertThat(actual.getPageData().number()).isEqualTo(pageData.getPageData().number());
        assertThat(actual.getPageData().numberOfElements()).isEqualTo(pageData.getPageData().numberOfElements());
        assertThat(actual.getPageData().totalElements()).isEqualTo(pageData.getPageData().totalElements());
        assertThat(actual.getSortData().field()).isEqualTo(pageData.getSortData().field());
        assertThat(actual.getSortData().asc()).isEqualTo(pageData.getSortData().asc());
    }

    @Test
    @DisplayName("Проверка получения списка лимитов. СМД. Подразделение указано")
    void test_getAll_dm_departmentDefined() {
        var departmentId = UUID.randomUUID();
        var employee = Instancio.create(Employee.class);
        var direction = Instancio.create(Direction.class);
        var sort = Instancio.create(String.class);
        var size = Instancio.create(Integer.class);
        var page = Instancio.create(Integer.class);
        var serviceType = Instancio.create(String.class);
        var status = Instancio.create(Status.class);
        var year = Instancio.create(Integer.class);
        var pageData = new Page<>(new PageImpl<>(Instancio.createList(Limit.class), PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort), 100));
        var department = Instancio.create(Department.class);
        var filter = LimitWebFilter.builder().organizationId(department.getOrganizationId()).departmentId(departmentId).year(year).status(status).serviceType(serviceType).build();

        when(departments.get(departmentId)).thenReturn(Optional.of(department));
        when(employees.current()).thenReturn(employee);
        when(employees.hasAccess()).thenReturn(true);
        when(employeeOrganizationFunction.apply(employee.getId())).thenReturn(employee.getOrganizationId());
        when(provider.get(department.getOrganizationId(), filter, page, size, sort, direction))
                .thenReturn(pageData);

        var actual = limits.get(filter, page, size, sort, direction);

        assertThat(actual.getContent()).hasSameElementsAs(pageData.getContent());
        assertThat(actual.getPageData().size()).isEqualTo(pageData.getPageData().size());
        assertThat(actual.getPageData().last()).isEqualTo(pageData.getPageData().last());
        assertThat(actual.getPageData().first()).isEqualTo(pageData.getPageData().first());
        assertThat(actual.getPageData().totalPages()).isEqualTo(pageData.getPageData().totalPages());
        assertThat(actual.getPageData().number()).isEqualTo(pageData.getPageData().number());
        assertThat(actual.getPageData().numberOfElements()).isEqualTo(pageData.getPageData().numberOfElements());
        assertThat(actual.getPageData().totalElements()).isEqualTo(pageData.getPageData().totalElements());
        assertThat(actual.getSortData().field()).isEqualTo(pageData.getSortData().field());
        assertThat(actual.getSortData().asc()).isEqualTo(pageData.getSortData().asc());
    }

    @Test
    @DisplayName("Проверка получения списка лимитов. Подразделение указано")
    void test_getAll_departmentDefined() {
        var departmentId = UUID.randomUUID();
        var employee = Instancio.create(Employee.class);
        var direction = Instancio.create(Direction.class);
        var sort = Instancio.create(String.class);
        var size = Instancio.create(Integer.class);
        var page = Instancio.create(Integer.class);
        var serviceType = Instancio.create(String.class);
        var status = Instancio.create(Status.class);
        var year = Instancio.create(Integer.class);
        var pageData = new Page<>(new PageImpl<>(Instancio.createList(Limit.class), PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort), 100));
        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), employee.getOrganizationId())
                .create();
        var filter = LimitWebFilter.builder().departmentId(departmentId).year(year).status(status).serviceType(serviceType).build();

        when(departments.get(departmentId)).thenReturn(Optional.of(department));
        when(employees.current()).thenReturn(employee);
        when(employeeOrganizationFunction.apply(employee.getId())).thenReturn(employee.getOrganizationId());
        when(provider.get(department.getOrganizationId(), filter, page, size, sort, direction))
                .thenReturn(pageData);

        var actual = limits.get(filter, page, size, sort, direction);

        assertThat(actual.getContent()).hasSameElementsAs(pageData.getContent());
        assertThat(actual.getPageData().size()).isEqualTo(pageData.getPageData().size());
        assertThat(actual.getPageData().last()).isEqualTo(pageData.getPageData().last());
        assertThat(actual.getPageData().first()).isEqualTo(pageData.getPageData().first());
        assertThat(actual.getPageData().totalPages()).isEqualTo(pageData.getPageData().totalPages());
        assertThat(actual.getPageData().number()).isEqualTo(pageData.getPageData().number());
        assertThat(actual.getPageData().numberOfElements()).isEqualTo(pageData.getPageData().numberOfElements());
        assertThat(actual.getPageData().totalElements()).isEqualTo(pageData.getPageData().totalElements());
        assertThat(actual.getSortData().field()).isEqualTo(pageData.getSortData().field());
        assertThat(actual.getSortData().asc()).isEqualTo(pageData.getSortData().asc());
    }

}