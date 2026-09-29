package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSpendingService;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsData;

import java.time.LocalDate;
import java.time.Month;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка модуля подготовки данных")
class LimitStatsDataImplTest {

    private final LimitService limitService = mock(LimitService.class);

    private final EmployeeService employeeService = mock(EmployeeService.class);

    private final LimitSpendingService limitSpendingService = mock(LimitSpendingService.class);

    private final LimitStatsData limitStatsData = new LimitStatsDataImpl(limitService, employeeService, limitSpendingService);

    @Test
    @DisplayName("Загрузка данных")
    void test_loadData() {
        var data = Instancio.ofList(LimitLevelDTO.class)
            .generate(Select.field(LimitLevelDTO::getLimit), gen -> gen.oneOf(Instancio.ofList(DepLimit.class).size(1000).create()))
            .create();
        var date = Instancio.create(LocalDate.class);
        var limitIds = data.parallelStream().map(LimitLevelDTO::getLimit).map(Limit::getId).collect(Collectors.toUnmodifiableSet());
        var departmentsMap = data.parallelStream()
            .map(l -> new AbstractMap.SimpleEntry<>(l.getLimit().getId(), ((DepLimit) l.getLimit()).getDepartment()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var employeesCount = departmentsMap.values().parallelStream().map(Department::getId)
                .map(depId -> new AbstractMap.SimpleEntry<>(depId, Instancio.create(Long.class)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var spendings = data.parallelStream()
                .map(l -> new AbstractMap.SimpleEntry<>(l.getLimit().getId(), Instancio.createList(LimitSpending.class)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        when(limitService.getDepartments(limitIds)).thenReturn(departmentsMap);
        when(employeeService.countByDepartmentsAndActive(departmentsMap.values().parallelStream().map(Department::getId).collect(Collectors.toUnmodifiableSet()), true))
            .thenReturn(employeesCount);
        when(limitSpendingService.getByLimitAndStatusNot(limitIds, LimitSpendingStatus.CANCELED)).thenReturn(spendings);

        limitStatsData.load(data, date);

        var limitId = limitIds.iterator().next();
        assertThat(limitStatsData.department(limitId)).isEqualTo(departmentsMap.get(limitId));

        var departmentId = departmentsMap.get(limitId).getId();
        assertThat(limitStatsData.employees(departmentId)).isEqualTo(employeesCount.get(departmentId));
        assertThat(limitStatsData.popSpends(limitId)).hasSameElementsAs(spendings.get(limitId));
        assertThat(limitStatsData.untilDate()).isEqualTo(date);
        assertThat(limitStatsData.startOfYear()).isEqualTo(LocalDate.of(date.getYear(), Month.JANUARY, 1));
    }

}