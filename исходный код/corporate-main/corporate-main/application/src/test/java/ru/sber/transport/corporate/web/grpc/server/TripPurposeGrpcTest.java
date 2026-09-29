package ru.sber.transport.corporate.web.grpc.server;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.TripPurpose;
import ru.sber.transport.corporate.business.providers.TripPurposeProvider;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.web.grpc.mappers.TripPurposeGrpcMapper;
import ru.sber.transport.corporate.web.grpc.mappers.TripPurposeGrpcMapperImpl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправки данных о целях поездки")
class TripPurposeGrpcTest {

    private final TripPurposeProvider provider = mock(TripPurposeProvider.class);

    private final TripPurposeGrpcMapper mapper = new TripPurposeGrpcMapperImpl();

    private final TripPurposeGrpc grpc = new TripPurposeGrpc(provider, mapper);

    @Test
    @DisplayName("Получение одного")
    void test_one() {
        var actualList = new ArrayList<OrganizationsOuterClass.TripPurpose>();
        var error = new AtomicReference<Throwable>();
        var completed = new AtomicBoolean();

        var data = Instancio.create(TripPurpose.class);

        when(provider.get(data.getId())).thenReturn(Optional.of(data));

        grpc.one(OrganizationsOuterClass.Request.newBuilder().setId(data.getId().toString()).build(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.TripPurpose value) {
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
                it.assertThat(actual.getLabel()).isEqualTo(data.getLabel());
                it.assertThat(actual.getId()).isEqualTo(data.getId().toString());
            });
        }
    }

}