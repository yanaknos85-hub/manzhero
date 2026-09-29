package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.DepartmentSender;
import ru.sber.transport.corporate.web.grpc.mappers.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправки данных о подразделениях")
class DepartmentGrpcTest {

    private final DepartmentProvider provider = mock(DepartmentProvider.class);

    private final DepartmentSender sender = mock(DepartmentSender.class);

    private final DepartmentGrpcMapper mapper = new DepartmentGrpcMapperImpl(new ActiveStatusGrpcMapperImpl(), new NullableMapperImpl());

    private final DepartmentGrpc grpc = new DepartmentGrpc(provider, mapper, sender);

    @Test
    @DisplayName("Получение всего")
    void test_all() {
        var actualList = new CopyOnWriteArrayList<OrganizationsOuterClass.Department>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.createList(Department.class);

        when(provider.get()).thenReturn(data);

        grpc.all(Empty.getDefaultInstance(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Department value) {
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

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = data.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId().toString());
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getCode()).isEqualTo(expected.getCode());
                it.assertThat(actual.getDeleted()).isEqualTo(Active.INACTIVE.equals(expected.getStatus()));
            });
        }
    }

    @Test
    @DisplayName("Получение одного")
    void test_one() {
        var actualList = new ArrayList<OrganizationsOuterClass.Department>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.create(Department.class);

        when(provider.get(data.getId())).thenReturn(Optional.of(data));

        grpc.one(OrganizationsOuterClass.Request.newBuilder().setId(data.getId().toString()).build(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Department value) {
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
                it.assertThat(actual.getName()).isEqualTo(data.getName());
                it.assertThat(actual.getCode()).isEqualTo(data.getCode());
                it.assertThat(actual.getDeleted()).isEqualTo(Active.INACTIVE.equals(data.getStatus()));
            });
        }
    }

}