package ru.sber.transport.limits.web.grpc.reservation;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.business.Reserves;
import ru.sber.transport.limits.business.exceptions.ReserveNotFoundException;
import ru.sber.transport.limits.business.exceptions.ReserveNotSufficientException;
import ru.sber.transport.limits.business.exceptions.ServiceNotAvailableException;
import ru.sber.transport.limits.business.exceptions.TypeNotAvailableException;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.limits.grpc.service.Limits;
import ru.sber.transport.limits.model.Reserve;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка делегата управления лимитами")
class LimitReservationTest {

    private final Reserves reserves = mock(Reserves.class);

    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

    private final LimitServiceGrpc.LimitServiceImplBase service = new LimitReservation(reserves, transactionManager);

    @Test
    @DisplayName("Проверка резервирования")
    void test_reserve_ok() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException {
        final var grpcResponse = new ArrayList<Limits.Response>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();

        final var first = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();
        final var second = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();
        final var third = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();
        final var fourth = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();

        doAnswer(inv -> {
            final var reserve = inv.getArgument(0, Reserve.class).id().toString();
            if (second.getId().equals(reserve)) {
                throw new ReserveNotSufficientException();
            }
            if (third.getId().equals(reserve)) {
                throw new TypeNotAvailableException("type");
            }
            if (fourth.getId().equals(reserve)) {
                throw new ServiceNotAvailableException("service");
            }
            return null;
        }).when(reserves).reserve(any());

        final var request = service.reserve(new StreamObserver<>() {

            @Override
            public void onNext(Limits.Response response) {
                grpcResponse.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });
        request.onNext(first);
        request.onNext(second);
        request.onNext(third);
        request.onNext(fourth);
        request.onCompleted();

        assertThat(grpcResponse).hasSize(4);
        assertThat(grpcResponse.stream().map(Limits.Response::getId).toList()).containsOnly(first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(grpcResponse.stream().map(Limits.Response::getStatus).toList()).containsOnly(Limits.Status.OK, Limits.Status.NOT_SUFFICIENT, Limits.Status.TYPE_NOT_AVAILABLE, Limits.Status.SERVICE_NOT_AVAILABLE);
        assertThat(error).hasNullValue();
        assertThat(completed).hasValue(true);

        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    @DisplayName("Проверка резервирования. Ошибка при резервировании")
    void test_reserve_error() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException {
        final var grpcResponse = new ArrayList<Limits.Response>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();

        final var first = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();
        final var second = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();
        final var third = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();
        final var fourth = Limits.ReserveRequest.newBuilder()
                .setConsumer(UUID.randomUUID().toString())
                .setCost(Limits.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)))
                .setType(Instancio.create(String.class))
                .setService(Instancio.create(String.class))
                .setDate(Limits.DateTime.newBuilder().setYear(Instancio.create(Integer.class)).setMonth(Instancio.create(Integer.class)).setDay(Instancio.create(Integer.class)).setHour(Instancio.create(Integer.class)).setMinute(Instancio.create(Integer.class)).setSecond(Instancio.create(Integer.class)).setMillis(Instancio.create(Integer.class)).setZone(Instancio.create(ZoneId.class).toString()).build())
                .setId(UUID.randomUUID().toString())
                .build();

        doAnswer(inv -> {
            final var reserve = inv.getArgument(0, Reserve.class).id().toString();
            if (second.getId().equals(reserve)) {
                throw new ReserveNotSufficientException();
            }
            if (third.getId().equals(reserve)) {
                throw new TypeNotAvailableException("type");
            }
            if (fourth.getId().equals(reserve)) {
                throw new ServiceNotAvailableException("service");
            }
            return null;
        }).when(reserves).reserve(any());

        final var request = service.reserve(new StreamObserver<>() {

            @Override
            public void onNext(Limits.Response response) {
                grpcResponse.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });
        request.onNext(first);
        request.onNext(second);
        request.onNext(third);
        request.onNext(fourth);
        request.onError(new RuntimeException("Test"));

        assertThat(grpcResponse).hasSize(4);
        assertThat(grpcResponse.stream().map(Limits.Response::getId).toList()).containsOnly(first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(grpcResponse.stream().map(Limits.Response::getStatus).toList()).containsOnly(Limits.Status.OK, Limits.Status.NOT_SUFFICIENT, Limits.Status.TYPE_NOT_AVAILABLE, Limits.Status.SERVICE_NOT_AVAILABLE);
        assertThat(error).hasNullValue();
        assertThat(completed).hasValue(true);

        verify(transactionManager, never()).commit(any());
        verify(transactionManager).rollback(any());
    }

    @Test
    @DisplayName("Проверка фиксирования резерва")
    void test_confirm_ok() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException, ReserveNotFoundException {
        final var grpcResponse = new ArrayList<Limits.Response>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();

        final var first = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var second = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();

        doAnswer(inv -> {
            final var reserve = inv.getArgument(0, UUID.class).toString();
            if (second.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            return null;
        }).when(reserves).confirm(any());

        final var request = service.confirm(new StreamObserver<>() {

            @Override
            public void onNext(Limits.Response response) {
                grpcResponse.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });
        request.onNext(first);
        request.onNext(second);
        request.onCompleted();

        assertThat(grpcResponse).hasSize(2);
        assertThat(grpcResponse.stream().map(Limits.Response::getId).toList()).containsOnly(first.getId(), second.getId());
        assertThat(grpcResponse.stream().map(Limits.Response::getStatus).toList()).containsOnly(Limits.Status.OK, Limits.Status.DATA_NOT_FOUND);
        assertThat(error).hasNullValue();
        assertThat(completed).hasValue(true);

        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    @DisplayName("Проверка фиксирования резерва. Ошибка при фиксировании")
    void test_confirm_error() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException, ReserveNotFoundException {
        final var grpcResponse = new ArrayList<Limits.Response>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();

        final var first = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var second = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var third = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var fourth = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();

        doAnswer(inv -> {
            final var reserve = inv.getArgument(0, UUID.class).toString();
            if (second.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            if (third.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            return null;
        }).when(reserves).confirm(any());

        final var request = service.confirm(new StreamObserver<>() {

            @Override
            public void onNext(Limits.Response response) {
                grpcResponse.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });
        request.onNext(first);
        request.onNext(second);
        request.onNext(third);
        request.onNext(fourth);
        request.onError(new RuntimeException("Test"));

        assertThat(grpcResponse).hasSize(4);
        assertThat(grpcResponse.stream().map(Limits.Response::getId).toList()).containsOnly(first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(grpcResponse.stream().map(Limits.Response::getStatus).toList()).containsOnly(Limits.Status.OK, Limits.Status.DATA_NOT_FOUND, Limits.Status.DATA_NOT_FOUND, Limits.Status.OK);
        assertThat(error).hasNullValue();
        assertThat(completed).hasValue(true);

        verify(transactionManager, never()).commit(any());
        verify(transactionManager).rollback(any());
    }

    @Test
    @DisplayName("Проверка отмены")
    void test_cancel_ok() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException, ReserveNotFoundException {
        final var grpcResponse = new ArrayList<Limits.Response>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();

        final var first = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var second = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var third = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var fourth = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();

        doAnswer(inv -> {
            final var reserve = inv.getArgument(0, UUID.class).toString();
            if (second.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            if (fourth.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            return null;
        }).when(reserves).cancel(any());

        final var request = service.cancel(new StreamObserver<>() {

            @Override
            public void onNext(Limits.Response response) {
                grpcResponse.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });
        request.onNext(first);
        request.onNext(second);
        request.onNext(third);
        request.onNext(fourth);
        request.onCompleted();

        assertThat(grpcResponse).hasSize(4);
        assertThat(grpcResponse.stream().map(Limits.Response::getId).toList()).containsOnly(first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(grpcResponse.stream().map(Limits.Response::getStatus).toList()).containsOnly(Limits.Status.OK, Limits.Status.DATA_NOT_FOUND, Limits.Status.OK, Limits.Status.DATA_NOT_FOUND);
        assertThat(error).hasNullValue();
        assertThat(completed).hasValue(true);

        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    @DisplayName("Проверка отмены. Ошибка при отмене")
    void test_cancel_error() throws TypeNotAvailableException, ServiceNotAvailableException, ReserveNotSufficientException, ReserveNotFoundException {
        final var grpcResponse = new ArrayList<Limits.Response>();
        final var error = new AtomicReference<Throwable>();
        final var completed = new AtomicReference<Boolean>();

        final var first = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var second = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var third = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();
        final var fourth = Limits.FixRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();

        doAnswer(inv -> {
            final var reserve = inv.getArgument(0, UUID.class).toString();
            if (second.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            if (third.getId().equals(reserve)) {
                throw new ReserveNotFoundException();
            }
            return null;
        }).when(reserves).cancel(any());

        final var request = service.cancel(new StreamObserver<>() {

            @Override
            public void onNext(Limits.Response response) {
                grpcResponse.add(response);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });
        request.onNext(first);
        request.onNext(second);
        request.onNext(third);
        request.onNext(fourth);
        request.onError(new RuntimeException("Test"));

        assertThat(grpcResponse).hasSize(4);
        assertThat(grpcResponse.stream().map(Limits.Response::getId).toList()).containsOnly(first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(grpcResponse.stream().map(Limits.Response::getStatus).toList()).containsOnly(Limits.Status.OK, Limits.Status.DATA_NOT_FOUND, Limits.Status.DATA_NOT_FOUND, Limits.Status.OK);
        assertThat(error).hasNullValue();
        assertThat(completed).hasValue(true);

        verify(transactionManager, never()).commit(any());
        verify(transactionManager).rollback(any());
    }

}
