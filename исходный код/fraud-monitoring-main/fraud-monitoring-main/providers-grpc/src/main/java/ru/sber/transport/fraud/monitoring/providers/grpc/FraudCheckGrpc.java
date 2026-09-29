package ru.sber.transport.fraud.monitoring.providers.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.fraud.monitoring.grpc.FraudCheckServiceGrpc;
import ru.sber.transport.fraud.monitoring.grpc.FraudInfo;
import ru.sber.transport.fraud.monitoring.grpc.FraudInfoRequest;
import ru.sber.transport.fraud.monitoring.grpc.FraudInfoResponse;
import ru.sber.transport.fraud.monitoring.providers.FraudsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FraudCheckGrpc extends FraudCheckServiceGrpc.FraudCheckServiceImplBase {

    private final TripRequestsDatabaseProvider tripRequestsDatabaseProvider;
    private final FraudsDatabaseProvider fraudsDatabaseProvider;

    @Override
    public void getFraudInfo(FraudInfoRequest request,
                             StreamObserver<FraudInfoResponse> responseObserver) {
        try {
            final var requestId = UUID.fromString(request.getRequestId());
            log.info("Processing fraud records request for requestId: {}", requestId);

            final var tripRequestOpt = tripRequestsDatabaseProvider.get(requestId);
            if (tripRequestOpt.isEmpty()) {
                log.warn("Trip request not found for requestId: {}", requestId);
                responseObserver.onNext(FraudInfoResponse.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }

            final var tripRequest = tripRequestOpt.get();
            final var frauds = fraudsDatabaseProvider.findByRequestId(requestId);

            final var records = frauds.stream()
                    .map(fraud -> FraudInfo.newBuilder()
                            .setId(fraud.getId().toString())
                            .setHumanReadableId(tripRequest.getHumanReadableId())
                            .setRequestId(fraud.getRequestId().toString())
                            .setPassengerId(tripRequest.getPassenger() != null ? tripRequest.getPassenger().getId().toString() : "")
                            .setFraudType(fraud.getFraudType())
                            .setComment(fraud.getComment() != null ? fraud.getComment() : "")
                            .build())
                    .toList();

            final var response = FraudInfoResponse.newBuilder()
                    .addAllRecords(records)
                    .build();

            log.info("Returning {} fraud records for requestId: {}", records.size(), requestId);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            log.warn("Invalid requestId format: {}", request.getRequestId(), e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid requestId format: " + e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            log.error("Error processing fraud records request for requestId: {}", request.getRequestId(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error: " + e.getMessage())
                            .asRuntimeException());
        }
    }
}