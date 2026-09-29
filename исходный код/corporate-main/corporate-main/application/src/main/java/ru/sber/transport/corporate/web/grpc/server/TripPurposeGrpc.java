package ru.sber.transport.corporate.web.grpc.server;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.corporate.business.providers.TripPurposeProvider;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.TripPurposesGrpc;
import ru.sber.transport.corporate.web.grpc.mappers.TripPurposeGrpcMapper;

import java.util.UUID;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class TripPurposeGrpc extends TripPurposesGrpc.TripPurposesImplBase {

    private final TripPurposeProvider provider;
    private final TripPurposeGrpcMapper mapper;

    @Override
    public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.TripPurpose> responseObserver) {
        var rawId = request.getId();
        log.info("Requested trip purpose {}", rawId);
        try {
            var id = UUID.fromString(rawId);
            var tripPurpose = provider.get(id)
                    .map(mapper::toGrpc)
                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND));
            responseObserver.onNext(tripPurpose);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Response to request trip purpose {} failed", rawId, e);
            responseObserver.onError(e);
        }
    }

}
