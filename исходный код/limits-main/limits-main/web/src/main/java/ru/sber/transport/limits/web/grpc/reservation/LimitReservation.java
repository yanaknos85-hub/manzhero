package ru.sber.transport.limits.web.grpc.reservation;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.sber.transport.limits.business.Reserves;
import ru.sber.transport.limits.business.exceptions.ReserveNotFoundException;
import ru.sber.transport.limits.business.exceptions.ReserveNotSufficientException;
import ru.sber.transport.limits.business.exceptions.ServiceNotAvailableException;
import ru.sber.transport.limits.business.exceptions.TypeNotAvailableException;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.limits.grpc.service.Limits;
import ru.sber.transport.limits.model.Reserve;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class LimitReservation extends LimitServiceGrpc.LimitServiceImplBase {

    private final Reserves reserves;

    private final PlatformTransactionManager transactionManager;

    private final Map<Long, TransactionStatus> transactions = new ConcurrentHashMap<>();

    @Override
    public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(Limits.ReserveRequest reserveRequest) {
                startTransaction();

                var status = Limits.Status.OK;
                try {
                    reserves.reserve(createReservation(reserveRequest));
                } catch (ReserveNotSufficientException e) {
                    status = Limits.Status.NOT_SUFFICIENT;
                } catch (TypeNotAvailableException e) {
                    log.warn("Type {} not available", reserveRequest.getType());
                    status = Limits.Status.TYPE_NOT_AVAILABLE;
                } catch (ServiceNotAvailableException e) {
                    log.warn("Service {} not available", reserveRequest.getService());
                    status = Limits.Status.SERVICE_NOT_AVAILABLE;
                }
                responseObserver.onNext(Limits.Response.newBuilder()
                        .setId(reserveRequest.getId())
                        .setStatus(status)
                        .build());
                log.debug("Reserve for {} added on thread {}", reserveRequest.getId(), Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onCompleted();
                rollbackTransaction();
                log.debug("Reserve rolled back on thread {}", Thread.currentThread().getName());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                commitTransaction();
                log.debug("Reserve commited back on thread {}", Thread.currentThread().getName());
            }
        };
    }

    private void commitTransaction() {
        transactionManager.commit(transactions.remove(Thread.currentThread().threadId()));
    }

    private void rollbackTransaction() {
        transactionManager.rollback(transactions.remove(Thread.currentThread().threadId()));
    }

    private void startTransaction() {
        final var id = Thread.currentThread().threadId();
        transactions.computeIfAbsent(id, k -> transactionManager.getTransaction(new DefaultTransactionDefinition()));
    }

    @Override
    public StreamObserver<Limits.FixRequest> confirm(StreamObserver<Limits.Response> responseObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(Limits.FixRequest fixRequest) {
                startTransaction();
                var status = Limits.Status.OK;
                try {
                    reserves.confirm(UUID.fromString(fixRequest.getId()));
                } catch (ReserveNotFoundException e) {
                    status = Limits.Status.DATA_NOT_FOUND;
                }
                responseObserver.onNext(Limits.Response.newBuilder()
                        .setId(fixRequest.getId())
                        .setStatus(status)
                        .build());
            }

            @Override
            public void onError(Throwable throwable) {
                rollbackTransaction();
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                commitTransaction();
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Limits.FixRequest> cancel(StreamObserver<Limits.Response> responseObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(Limits.FixRequest fixRequest) {
                startTransaction();
                var status = Limits.Status.OK;
                try {
                    reserves.cancel(UUID.fromString(fixRequest.getId()));
                } catch (ReserveNotFoundException e) {
                    status = Limits.Status.DATA_NOT_FOUND;
                }
                responseObserver.onNext(Limits.Response.newBuilder()
                        .setId(fixRequest.getId())
                        .setStatus(status)
                        .build());
            }

            @Override
            public void onError(Throwable throwable) {
                rollbackTransaction();
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                commitTransaction();
                responseObserver.onCompleted();
            }
        };
    }

    private Reserve createReservation(Limits.ReserveRequest reserveRequest) {
        return new Reserve() {

            @Override
            public BigDecimal cost() {
                final var grpcCost = reserveRequest.getCost();
                return new BigDecimal(String.format("%s.%s", grpcCost.getIntegerPart(), grpcCost.getFractionPart()));
            }

            @Override
            public UUID id() {
                return UUID.fromString(reserveRequest.getId());
            }

            @Override
            public String service() {
                return reserveRequest.getService();
            }

            @Override
            public String type() {
                return reserveRequest.getType();
            }

            @Override
            public UUID consumerId() {
                return UUID.fromString(reserveRequest.getConsumer());
            }

            @Override
            public OffsetDateTime date() {
                final var date = reserveRequest.getDate();
                return OffsetDateTime.of(
                        date.getYear(),
                        date.getMonth(),
                        date.getDay(),
                        date.getHour(),
                        date.getMinute(),
                        date.getSecond(),
                        date.getMillis(),
                        ZoneOffset.of(date.getZone())
                );
            }
        };
    }
}
