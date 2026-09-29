package ru.sber.transport.corporate.web.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.DelegateSender;
import ru.sber.transport.corporate.model.Delegate;
import ru.sber.transport.corporate.providers.Delegates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
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
@DisplayName("Проверка отправки данных о делегатах")
class DelegatesGrpcTest {

    private final Delegates provider = mock(Delegates.class);

    private final DelegateSender sender = mock(DelegateSender.class);

    private final DelegatesGrpc grpc = new DelegatesGrpc(provider, sender);

    @Test
    @DisplayName("Получение всего")
    void test_all() {
        var actualList = new CopyOnWriteArrayList<OrganizationsOuterClass.Delegate>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.createList(TestDelegate.class).stream().map(Delegate.class::cast).toList();

        when(provider.get(0, 20)).thenReturn(data);
        when(provider.count()).thenReturn((long) data.size());

        grpc.all(Empty.getDefaultInstance(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Delegate value) {
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
                it.assertThat(actual.getDelegateId()).isEqualTo(expected.delegateId().toString());
                it.assertThat(actual.getType()).isEqualTo(expected.type());
                it.assertThat(actual.getSupervisorId()).isEqualTo(expected.supervisorId().toString());
            });
        }
    }

    @Test
    @DisplayName("Получение одного")
    void test_one() {
        var actualList = new ArrayList<OrganizationsOuterClass.Delegate>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.create(TestDelegate.class);

        when(provider.get(data.id())).thenReturn(Optional.of(data));

        grpc.one(OrganizationsOuterClass.Request.newBuilder().setId(data.id().toString()).build(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Delegate value) {
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
                it.assertThat(actual.getDelegateId()).isEqualTo(data.delegateId().toString());
                it.assertThat(actual.getType()).isEqualTo(data.type());
                it.assertThat(actual.getSupervisorId()).isEqualTo(data.supervisorId().toString());
            });
        }
    }

    private record TestDelegate(
            UUID id,
            UUID delegateId,
            UUID supervisorId,
            LocalDate startDate,
            LocalDate endDate,
            String type,
            UUID typeId,
            boolean deleted
    ) implements Delegate {}

}