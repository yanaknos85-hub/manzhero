package ru.sber.transport.corporate.web.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.val;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка делегата классов такси")
class PositionTaxiClassesDelegateImplTest {

    private final EmployeeProvider employeeProvider = mock(EmployeeProvider.class);

    private final AvailableClassesProvider availableClassesProvider = mock(AvailableClassesProvider.class);

    private final PositionTaxiClassesDelegateImpl delegate = new PositionTaxiClassesDelegateImpl(employeeProvider,
        availableClassesProvider);

    @Test
    @DisplayName("Получение классов такси пользователя")
    void taxiClassesGet() {
        val employee = Instancio.create(Employee.class);

        when(employeeProvider.getByIdOrPersonalNumber(eq(employee.getId()), any()))
            .thenReturn(Optional.of(employee));

        when(availableClassesProvider.get(employee.getPositionId()))
            .thenReturn(Set.of("COMFORT"));

        val result = delegate.taxiClassesGet(employee.getId(), null);

        assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("body", List.of("COMFORT"));
    }

    @Test
    @DisplayName("Получение классов такси пользователя с пустыми параметрами")
    void givenNoQueryParamtaxiClassesGet() {
        val employee = Instancio.create(Employee.class);

        when(employeeProvider.getByIdOrPersonalNumber(any(), any()))
            .thenReturn(Optional.of(employee));

        when(availableClassesProvider.getAll())
            .thenReturn(Set.of("COMFORT"));

        val result = delegate.taxiClassesGet(null, null);

        assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("body", List.of("COMFORT"));
        then(employeeProvider).shouldHaveNoInteractions();
        then(availableClassesProvider).should(times(0)).get(any());
    }

}