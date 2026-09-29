package ru.sberbank.ditsib.transport.limits.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.limits.grpc.dto.cargo.MassLimitReservationModel;
import ru.sber.transport.limits.grpc.service.MassLimitReservationServiceGrpc;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.grpc.mapper.MassLimitActionMapper;
import ru.sberbank.ditsib.transport.limits.service.LimitActionService;

import java.util.Collections;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.limits.model.LimitReservationStatus.LIMIT_RESERVATION_FAILED;

/**
 * Реализация RPC сервиса.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class MassLimitReservationRpcServiceImpl extends MassLimitReservationServiceGrpc.MassLimitReservationServiceImplBase {

    private final LimitActionService limitActionService;

    private final MassLimitActionMapper limitActionMapper;

    public static final String MSG_FORMAT = "Ошибка при обработке запроса";

    @Override
    public void massLimitReservation(
        MassLimitReservationModel.MassLimitReservationRequestList request,
        StreamObserver<MassLimitReservationModel.MassLimitReservationResponseList> responseObserver
    ) {

        if (request.getListLimitReservationsList().isEmpty()) {
            processAnswer(MassLimitActionResultMessage.builder()
                    .resultMessages(Collections.emptyList())
                    .limitReservationStatus(LIMIT_RESERVATION_FAILED.name())
                    .build(),
                responseObserver);
            return;
        }

        var action = request.getAction();
        var reservationStrategy = request.getReservationStrategy();
        var actionMessages = request.getListLimitReservationsList().stream().map(limitActionMapper::map).toList();
        var massLimitReservationsList = new MassLimitActionMessage(actionMessages, action, reservationStrategy);
        var userId = UUID.fromString(request.getUserId());

        try {
            var limitActionResultMessage =
                limitActionService.processMassLimitAction(userId, massLimitReservationsList);

            processAnswer(limitActionResultMessage, responseObserver);
        } catch (Exception e) {
            responseObserver.onError(e);
            log.warn("Request for reservation failed. {}, {}", e.getClass(), e.getMessage());
        }
    }

    @Override
    public void limitReservationCancel(MassLimitReservationModel.LimitReservationRequest request, StreamObserver<MassLimitReservationModel.LimitReservationResponse> responseObserver) {

        try {var msg = limitActionMapper.toDoCancel(request);
            var result = limitActionService.processLimitAction(msg);
            processCancelAnswer(result, responseObserver);
            log.info("Limit reservation cancelled successfully.");
        } catch (Exception e) {
            responseObserver.onError(e);
            log.warn("Request for reservation cancelling failed. {}, {}", e.getClass(), e.getMessage());
        }
    }

    private void processAnswer(
        MassLimitActionResultMessage massLimitActionResultMessage,
        StreamObserver<MassLimitReservationModel.MassLimitReservationResponseList> responseObserver) {

        try {
            var response = MassLimitReservationModel.MassLimitReservationResponseList.newBuilder()
                .setMessage(limitActionMapper.getNullableString(massLimitActionResultMessage.message()))
                .setLimitReservationStatus(massLimitActionResultMessage.limitReservationStatus())
                .addAllLimitReservationResponse(massLimitActionResultMessage.resultMessages().stream().map(limitActionMapper::map).toList())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error(MSG_FORMAT, e);
            responseObserver.onError(e);
        }
    }

    private void processCancelAnswer(
        LimitActionResultMessage limitActionResultMessage,
        StreamObserver<MassLimitReservationModel.LimitReservationResponse> responseObserver) {

        try {
            var response = limitActionMapper.map(limitActionResultMessage);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error(MSG_FORMAT, e);
            responseObserver.onError(e);
        }
    }

}