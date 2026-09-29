package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.EmployeeSender;
import ru.sber.transport.corporate.web.grpc.mappers.*;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправки данных о сотрудниках")
class EmployeeGrpcTest {

    private final Provider<Employee, EmployeeFilter> provider = mock(Provider.class);

    private final EmployeeGrpcMapper mapper = new EmployeeGrpcMapperImpl(new ActiveStatusGrpcMapperImpl(), new NullableMapperImpl(), new GenderGrpcMapperImpl());

    private final EmployeeSender sender = mock(EmployeeSender.class);

    private final EmployeeGrpc grpc = new EmployeeGrpc(provider, mapper, sender);

    @BeforeEach
    void init() {
        mapper.setContactsGrpcMapper(new ContactsGrpcMapperImpl());
    }

    @Test
    @DisplayName("Получение всего")
    void test_all() {
        var actualList = new CopyOnWriteArrayList<OrganizationsOuterClass.Employee>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.createList(Employee.class);

        when(provider.get()).thenReturn(data);

        grpc.all(Empty.getDefaultInstance(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Employee value) {
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

        assertThat(actualList).hasSameSizeAs(data);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        actualList.sort(Comparator.comparing(OrganizationsOuterClass.Employee::getPersonnelNumber));
        data.sort(Comparator.comparing(Employee::getPersonnelNumber));

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = data.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId().toString());
                it.assertThat(actual.getConsent()).isEqualTo(expected.isConsent());
                it.assertThat(actual.getDepartmentId()).isEqualTo(expected.getDepartmentId().toString());
                it.assertThat(actual.getPositionId()).isEqualTo(expected.getPositionId().toString());
                it.assertThat(actual.getOrganizationId()).isEqualTo(expected.getOrganizationId().toString());
                it.assertThat(actual.getEmployeeType().name()).isEqualTo(expected.getStructureType().name());
                it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
                it.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
                it.assertThat(actual.getPatronymic().getValue()).isEqualTo(expected.getPatronymic());
                it.assertThat(actual.getItinerantType().name()).isEqualTo(expected.getItinerant().name());
                it.assertThat(actual.getCostCenter().getValue()).isEqualTo(expected.getCostCenter());
                it.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
                it.assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getPersonnelNumber());
                it.assertThat(actual.getDeleted()).isEqualTo(Active.INACTIVE.equals(expected.getStatus()));
                it.assertThat(actual.getContactsList()).hasSize(3);
            });
        }
    }

    @Test
    @DisplayName("Получение одного")
    void test_one() {
        var actualList = new CopyOnWriteArrayList<OrganizationsOuterClass.Employee>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.create(Employee.class);

        when(provider.get(data.getId())).thenReturn(Optional.of(data));

        grpc.one(OrganizationsOuterClass.Request.newBuilder().setId(data.getId().toString()).build(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Employee value) {
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

        assertThat(actualList).hasSize(1);
        assertThat(error.get()).isNull();
        assertThat(completed.get()).isTrue();

        for (var actual : actualList) {
            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(data.getId().toString());
                it.assertThat(actual.getConsent()).isEqualTo(data.isConsent());
                it.assertThat(actual.getDepartmentId()).isEqualTo(data.getDepartmentId().toString());
                it.assertThat(actual.getPositionId()).isEqualTo(data.getPositionId().toString());
                it.assertThat(actual.getOrganizationId()).isEqualTo(data.getOrganizationId().toString());
                it.assertThat(actual.getEmployeeType().name()).isEqualTo(data.getStructureType().name());
                it.assertThat(actual.getFirstName()).isEqualTo(data.getFirstName());
                it.assertThat(actual.getLastName()).isEqualTo(data.getLastName());
                it.assertThat(actual.getPatronymic().getValue()).isEqualTo(data.getPatronymic());
                it.assertThat(actual.getItinerantType().name()).isEqualTo(data.getItinerant().name());
                it.assertThat(actual.getCostCenter().getValue()).isEqualTo(data.getCostCenter());
                it.assertThat(actual.getHumanReadableId()).isEqualTo(data.getHumanReadableId());
                it.assertThat(actual.getPersonnelNumber()).isEqualTo(data.getPersonnelNumber());
                it.assertThat(actual.getDeleted()).isEqualTo(Active.INACTIVE.equals(data.getStatus()));
                it.assertThat(actual.getContactsList()).hasSize(3);
            });
        }
    }

}