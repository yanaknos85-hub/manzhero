package ru.sberbank.ditsib.transport.limits.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.limits.grpc.dto.LimitReservationModel;
import ru.sber.transport.limits.grpc.service.LimitReservationServiceGrpc;
import ru.sberbank.ditsib.transport.dto.limits.ReservationStrategy;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.grpc.mapper.LimitActionMapper;
import ru.sberbank.ditsib.transport.limits.service.LimitActionService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Реализация RPC сервиса.
 */
@Slf4j
@RequiredArgsConstructor
@GrpcService
public class LimitReservationRpcServiceImpl extends LimitReservationServiceGrpc.LimitReservationServiceImplBase {

    private final LimitActionService limitActionService;

    private final LimitActionMapper limitActionMapper;

    @Override
    public void limitReservation(
        LimitReservationModel.LimitReservationRequestList request,
        StreamObserver<LimitReservationModel.LimitReservationResponseList> responseObserver
    ) {

        if (request.getListLimitReservationsList().isEmpty()) {
            processAnswer(Collections.emptyList(), responseObserver);
            return;
        }

        var limitReservationsList = request.getListLimitReservationsList().stream().map(limitActionMapper::map).toList();

        UUID userId = UUID.fromString(request.getUserId());
        try {
            var limitActionResultMessage =
                limitActionService.processMassLimitAction(userId, limitReservationsList, ReservationStrategy.ANY, "RESERVE");
            processAnswer(limitActionResultMessage.resultMessages(), responseObserver);
        } catch (Exception e) {
            log.warn("Request failed. {}, {}", e.getClass(), e.getMessage());
            responseObserver.onError(e);
        }
    }

    private void processAnswer(
        List<LimitActionResultMessage> resultMessages,
        StreamObserver<LimitReservationModel.LimitReservationResponseList> responseObserver) {

        var limitReservationResponses = resultMessages.stream()
            .map(limitActionMapper::map).toList();

        LimitReservationModel.LimitReservationResponseList response = LimitReservationModel.LimitReservationResponseList.newBuilder()
            .addAllLimitReservationResponse(limitReservationResponses)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}