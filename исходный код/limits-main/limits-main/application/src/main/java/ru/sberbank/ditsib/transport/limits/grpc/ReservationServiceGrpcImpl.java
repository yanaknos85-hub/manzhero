package ru.sberbank.ditsib.transport.limits.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.grpc.mapper.LimitActionMapper;
import ru.sberbank.ditsib.transport.limits.service.LimitActionService;

@Slf4j
@RequiredArgsConstructor
@GrpcService
class ReservationServiceGrpcImpl extends LimitReservationServiceGrpc.LimitReservationServiceImplBase {

    private final LimitActionService limitActionService;

    private final LimitActionMapper limitActionMapper;

    private final PlatformTransactionManager manager;

    @Override
    public void limitReservation(
        OldReserve.LimitReservationRequest request,
        StreamObserver<OldReserve.LimitReservationResponse> responseObserver
    ) {
        new TransactionTemplate(manager).executeWithoutResult(status -> {
            var limitReservation = limitActionMapper.map(request);

            var limitActionResultMessage =
                limitActionService.processLimitAction(limitReservation);

            processAnswer(limitActionResultMessage, responseObserver);
        });
    }

    private void processAnswer(
        LimitActionResultMessage resultMessage,
        StreamObserver<OldReserve.LimitReservationResponse> responseObserver) {

        var limitReservationResponses = limitActionMapper.mapNext(resultMessage);

        responseObserver.onNext(limitReservationResponses);
        responseObserver.onCompleted();
    }
}
