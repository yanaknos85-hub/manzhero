package ru.sber.transport.corporate.web.grpc.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.providers.AvailableClassesProvider;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.grpc.service.TaxiClasses.EmployeeIdInfoRequest;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправки данных о классах такси")
class PositionTaxiClassesGrpcTest {

    private final EmployeeProvider employeeProvider = mock(EmployeeProvider.class);

    private final AvailableClassesProvider availableClassesProvider = mock(AvailableClassesProvider.class);

    private final PositionTaxiClassesGrpc grpc = new PositionTaxiClassesGrpc(employeeProvider,
        availableClassesProvider);

    @Test
    @DisplayName("Получение классов для пользователя по ID")
    void givenEmployeeId_whenAllForEmployee_ThenSuccess() {

        var actualList = new ArrayList<StringValue>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();
        var employee = Instancio.create(Employee.class);

        when(employeeProvider.getByIdOrPersonalNumber(eq(employee.getId()), any())).thenReturn(Optional.of(employee));
        when(availableClassesProvider.get(employee.getPositionId())).thenReturn(Set.of("A", "C"));

        grpc.allForEmployee(
            EmployeeIdInfoRequest.newBuilder()
                .setId(StringValue.of(employee.getId().toString()))
                .build(), new StreamObserver<>() {

                @Override
                public void onNext(StringValue value) {
                    actualList.add(value);
                }

                @Override
                public void onError(Throwable t) {
                    error.set(t);
                }

                @Override
                public void onCompleted() {
                    completed.set(true);
                }
            });

        assertThat(actualList)
            .hasSize(2)
            .containsOnly(StringValue.of("A"), StringValue.of("C"));
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

    @Test
    @DisplayName("Получение классов для пользователя по ПН")
    void givenEmployeePersonaNumber_whenAllForEmployee_ThenSuccess() {

        var actualList = new ArrayList<StringValue>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();
        var employee = Instancio.create(Employee.class);

        when(employeeProvider.getByIdOrPersonalNumber(any(), eq(employee.getPersonnelNumber()))).thenReturn(
            Optional.of(employee));
        when(availableClassesProvider.get(employee.getPositionId())).thenReturn(Set.of("A", "C"));

        grpc.allForEmployee(
            EmployeeIdInfoRequest.newBuilder()
                .setPersonnelNumber(StringValue.of(employee.getPersonnelNumber()))
                .build(), new StreamObserver<>() {

                @Override
                public void onNext(StringValue value) {
                    actualList.add(value);
                }

                @Override
                public void onError(Throwable t) {
                    error.set(t);
                }

                @Override
                public void onCompleted() {
                    completed.set(true);
                }
            });

        assertThat(actualList)
            .hasSize(2)
            .containsOnly(StringValue.of("A"), StringValue.of("C"));
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();
    }

}