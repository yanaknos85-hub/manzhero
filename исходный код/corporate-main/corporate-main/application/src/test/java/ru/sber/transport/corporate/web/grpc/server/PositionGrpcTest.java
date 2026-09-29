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
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.PositionSender;
import ru.sber.transport.corporate.web.grpc.mappers.*;

import java.util.ArrayList;
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
@DisplayName("Проверка отправки данных о должностях")
class PositionGrpcTest {

    private final Provider<Position, PositionFilter> provider = mock(Provider.class);

    private final PositionGrpcMapper mapper = new PositionGrpcMapperImpl(new ActiveStatusGrpcMapperImpl(), new NullableMapperImpl());

    private final PositionSender sender = mock(PositionSender.class);

    private final PositionGrpc grpc = new PositionGrpc(provider, mapper, sender);

    @Test
    @DisplayName("Получение всего")
    void test_all() {
        var actualList = new CopyOnWriteArrayList<OrganizationsOuterClass.Position>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.createList(Position.class);

        when(provider.get()).thenReturn(data);

        grpc.all(Empty.getDefaultInstance(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Position value) {
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

        actualList.sort(Comparator.comparing(OrganizationsOuterClass.Position::getName));
        data.sort(Comparator.comparing(Position::getName));

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = data.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId().toString());
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getDeleted()).isEqualTo(Active.INACTIVE.equals(expected.getStatus()));
            });
        }
    }

    @Test
    @DisplayName("Получение одного")
    void test_one() {
        var actualList = new ArrayList<OrganizationsOuterClass.Position>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.create(Position.class);

        when(provider.get(data.getId())).thenReturn(Optional.of(data));

        grpc.one(OrganizationsOuterClass.Request.newBuilder().setId(data.getId().toString()).build(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Position value) {
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
                it.assertThat(actual.getDeleted()).isEqualTo(Active.INACTIVE.equals(data.getStatus()));
            });
        }
    }

}