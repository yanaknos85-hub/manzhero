package ru.sber.transport.corporate.web.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.NullValue;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.DelegateSender;
import ru.sber.transport.corporate.model.Delegate;
import ru.sber.transport.corporate.providers.Delegates;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@GrpcService
public class DelegatesGrpc extends ru.sber.transport.corporate.grpc.service.DelegatesGrpc.DelegatesImplBase {

    private static final Logger log = LoggerFactory.getLogger(DelegatesGrpc.class);

    private final Delegates provider;

    private final DelegateSender sender;

    public DelegatesGrpc(Delegates provider, DelegateSender sender) {
        log.info("Creating delegates gRPC service");
        this.provider = provider;
        this.sender = sender;
    }

    @Override
    public void all(Empty request, StreamObserver<OrganizationsOuterClass.Delegate> responseObserver) {
        log.info("Requested all organizations");
        try {
            final var count = provider.count();
            final var size = 20;
            var current = 0;
            do {
                provider.get(current, size)
                        .stream()
                        .map(this::toGrpc)
                        .forEach(responseObserver::onNext);
                current += size;
            } while (current < count);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Responding failed", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.Delegate> responseObserver) {
        var rawId = request.getId();
        log.info("Requested organization {}", rawId);
        try {
            var id = UUID.fromString(rawId);
            var organization = provider.get(id)
                    .map(this::toGrpc)
                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND));
            responseObserver.onNext(organization);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Response to request organization {} failed", rawId, e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void refresh(Empty request, StreamObserver<Empty> responseObserver) {
        final var count = provider.count();
        final var size = 20;
        var current = 0;
        do {
            provider.get(current, size).forEach(sender::send);
            current += size;
        } while (current < count);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private OrganizationsOuterClass.Delegate toGrpc(Delegate source) {
        return OrganizationsOuterClass.Delegate.newBuilder()
                .setDelegateId(source.delegateId().toString())
                .setSupervisorId(source.supervisorId().toString())
                .setStartDate(toGrpc(source.startDate()))
                .setEndDate(toNullableGrpc(source.endDate()))
                .setType(source.type())
                .setActive(!source.deleted())
                .build();
    }

    private OrganizationsOuterClass.NullableDate toNullableGrpc(LocalDate source) {
        return Optional.ofNullable(source)
                .map(it -> OrganizationsOuterClass.NullableDate.newBuilder().setValue(toGrpc(it)).build())
                .orElseGet(() -> OrganizationsOuterClass.NullableDate.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }

    private OrganizationsOuterClass.Date toGrpc(LocalDate source) {
        return OrganizationsOuterClass.Date.newBuilder()
                .setYear(source.getYear())
                .setMonth(source.getMonth().getValue())
                .setDay(source.getDayOfMonth())
                .build();
    }
}
