package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.mappers.TripApprovalMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveTripRequestSender;
import ru.sberbank.ditsib.transport.approvals.services.*;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.approvals.database.model.Status.*;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestApprovalServiceImpl implements RequestApprovalService {

    public static final String SHARED_RIDE_APPROVE_NAME = "SHARED RIDE";
    public static final String REQUEST_APPROVE_NAME = "REQUEST";
    public static final String FINAL_TRIP_APPROVE_NAME = "FINAL TRIP";
    private final ApproveTripRequestService approveTripRequestService;
    private final ApproveService<FinalTripApproval> finalTripApproveService;
    private final ApproveUpdateTripRequestService updateTripRequestApprovalApproveService;
    private final SharedRideApproveService sharedRideApproveService;
    private final TripApprovalMapper mapper;
    private final ApproveTripRequestSender approveSender;
    private final TripApproverService tripApproverService;
    private final EmployeeService employeeService;
    private final FraudDataService fraudDataService;

    private static final List<TripRequestStatus> FINAL_APPROVE_STATUSES = Arrays.asList(PERSONAL_AWAITING_TRIP_APPROVAL,
            PUBLIC_AWAITING_AFFIRMATIVE,
            CARSHARING_AWAITING_TRIP_APPROVAL);
    private static final List<TripRequestStatus> AWAITING_STATUSES = Arrays.asList(TAXI_AWAITING_APPROVAL,
            PERSONAL_AWAITING_TRIP_APPROVAL,
            PUBLIC_AWAITING_APPROVAL,
            CARSHARING_AWAITING_APPROVAL,
            CARGO_AWAITING_APPROVAL,
            REPAIR_AWAITING_APPROVAL,
            GROUP_TRANSFER_AWAITING_APPROVAL);

    @Override
    @Transactional
    public void handleRequestMessage(RequestMessage message) {
        try {
            final var requestId = message.getId();
            final var tripRequestStatus = TripRequestStatus.valueOf(message.getStatus());
            Objects.requireNonNull(requestId, "Request message id is null");
            Objects.requireNonNull(tripRequestStatus, "Request status is null");
            for (var transportCompensation : message.getTransportCompensation()) {
                log.info(transportCompensation.getCompensationType());
            }
            boolean isSuburbTrip = isSuburbTrip(message);
            var passenger = message.getPassenger() == null
                    ? null
                    : employeeService.getByUserId(message.getPassenger().getUserId());
            handleRequestApproval(message, requestId, tripRequestStatus, isSuburbTrip, passenger);
            handleUpdateTripRequestApproval(message, requestId);
            handleFinalTripApproval(message, requestId, tripRequestStatus, isSuburbTrip, passenger);
            handleSharedRideApproval(message, requestId, tripRequestStatus, passenger);
        } catch (Exception e) {
            log.error("Unable save request message: {}", e.getMessage(), e);
        }
    }

    /**
     * Создание/изменение согласования заявки на маршрут
     */
    private void handleRequestApproval(RequestMessage message, UUID requestId, TripRequestStatus tripRequestStatus,
                                       boolean isSuburbTrip, Employee passenger) {
        final var approval = approveTripRequestService.findOrCreate(requestId);
        log.info("handleRequestMessage: received RequestMessage with id {} and status of approval {}", requestId, approval.getStatus());
        approval.setSuburbTrip(isSuburbTrip);
        if (TripRequestStatus.getApprovedStatuses().contains(tripRequestStatus) && (approval.getStatus() != APPROVED)) {
            // Предполагается, что согласование может быть сделан только в данном МС, а значит данный статус должен быть уже выставлен
            log.warn("handleRequestMessage: Approval with id '{}' not approved (no flow influence)", approval.getId());
        }
        var status = getStatus(approval, message.isDeleted(), tripRequestStatus, tripRequestStatus.isApprovable());
        if (status == null) {
            logSkip(requestId, tripRequestStatus.name(), REQUEST_APPROVE_NAME, "status logic");
            return;
        }
        mapper.toTripRequestApproval(approval, message);

        handleFraudData(message, approval);

        approval.setStatus(status);
        approval.setPassenger(passenger);

        if (message.isDeleted()) {
            approveTripRequestService.cancel(approval);
        } else {
            approveTripRequestService.save(approval);
            var savedApproval = approveTripRequestService.findApprovalByActionId(approval.getActionId());
            if (savedApproval.isEmpty()) {
                log.warn("handleRequestMessage: Approval with action id '{}' not found", approval.getActionId());
                return;
            }
            log.info("handleRequestMessage: saved RequestMessage with id {} and status of approval {}", requestId,
                    approval.getStatus());
            if (AWAITING_STATUSES.contains(tripRequestStatus) &&
                    !(APPROVED.equals(savedApproval.get().getStatus()))) {
                log.debug("handleRequestMessage: going to send trip request approval with id {}", approval.getId());
                sendToApprovers(message, requestId);
            }
        }
    }

    /**
     * Обработка сообщения об изменении заявки: если заявка была удален, то удаляем и согласование на изменение
     */
    private void handleUpdateTripRequestApproval(RequestMessage message, UUID requestId) {
        if (message.isDeleted()) {
            updateTripRequestApprovalApproveService.cancelByRequestId(requestId);
        }
    }

    private void handleFinalTripApproval(RequestMessage message, UUID requestId, TripRequestStatus tripRequestStatus,
                                         boolean isSuburbTrip, Employee passenger) {
        final var approval = finalTripApproveService.findOrCreate(requestId);
        approval.setSuburbTrip(isSuburbTrip);
        var status = getStatus(approval, message.isDeleted(), tripRequestStatus, FINAL_APPROVE_STATUSES.contains(tripRequestStatus));
        if (status == null) {
            logSkip(requestId, tripRequestStatus.name(), FINAL_TRIP_APPROVE_NAME, "status logic");
            return;
        }
        approval.setStatus(status);
        mapper.toBaseRequestApproval(message, approval);
        handleFraudData(message, approval);
        approval.setPassenger(passenger);
        if (message.isDeleted()) {
            finalTripApproveService.cancel(approval);
        } else {
            finalTripApproveService.save(approval);
        }
    }

    /**
     * Обрабатывает сообщения как присоединяемых заявок, так и владельцев личного транспорта
     *
     * @param message {@link RequestMessage}
     * @param tripRequestStatus {@link TripRequestStatus}
     * @param passenger {@link Employee}
     */
    private void handleSharedRideApproval(RequestMessage message, UUID requestId, TripRequestStatus tripRequestStatus,
                                          Employee passenger) {
        if (TransportTypeEnum.PERSONAL != TransportTypeEnum.valueOf(message.getTransportType())) {
            logSkip(message.getId(), tripRequestStatus.name(), SHARED_RIDE_APPROVE_NAME,
                    "transport type is " + message.getTransportType());
            return;
        }
        handleSharedRideApprovalForAddedRequest(message, requestId, tripRequestStatus, passenger);
        // На будущее для оптимизации можно будет поставить условие, что если сработал handler для присоединяемой
        // поездки не запускать обработчик для заявок владельцев личного транспорта
        handleSharedRideApprovalForOwnerRequest(message, requestId, tripRequestStatus, passenger);
    }


    private void handleFraudData(RequestMessage message, Approval approval) {
        log.debug("[handleFraudData]: fraud data is {}", message.getFraudData());
        fraudDataService.updateFraudDataForApproval(approval, message.getFraudData());
    }

    /**
     * Проверяет есть ли в списке компенсация междугородних поездок
     */
    private boolean isSuburbTrip(RequestMessage message) {
        for (var compensation : message.getTransportCompensation()) {
            var compensationType = PublicCompensationType.getByName(compensation.getCompensationType())
                    .orElse(null);
            if (compensationType != null && compensationType.equals(PublicCompensationType.SUBURB_TRIP_COMPENSATION)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Обработка сообщения для случая, когда заявка является владельцем личного транспорта
     * @param message {@link RequestMessage}
     * @param tripRequestStatus {@link TripRequestStatus}
     * @param passenger {@link Employee}
     */
    private void handleSharedRideApprovalForOwnerRequest(RequestMessage message, UUID requestId, TripRequestStatus tripRequestStatus, Employee passenger) {
        log.debug("STEP 9: handleSharedRideApprovalForOwnerRequest entered");
        sharedRideApproveService.findApprovalByActionId(requestId).ifPresent(approval -> {
            var status = getStatus(approval, message.isDeleted(), tripRequestStatus, true);
            log.debug("STEP 10: status {}", status);
            if (status == null) {
                logSkip(requestId, tripRequestStatus.name(), SHARED_RIDE_APPROVE_NAME, "status logic for owner request");
                return;
            }
            mapper.toBaseRequestApproval(message, approval);
            approval.setPassenger(passenger);
            approval.setStatus(status);
            log.debug("STEP 11: saving sharedRide, approval {}", approval);
            sharedRideApproveService.save(approval);
            handleFraudData(message, approval);
        });
    }

    /**
     * Обработка сообщения для случая, когда заявка является присоединяемой
     *
     * @param message {@link RequestMessage}
     * @param tripRequestStatus {@link TripRequestStatus}
     * @param passenger {@link Employee}
     */
    private void handleSharedRideApprovalForAddedRequest(RequestMessage message, UUID requestId, TripRequestStatus tripRequestStatus, Employee passenger) {
        log.debug("STEP 2: handleSharedRideApprovalForAddedRequest entered");
        // Ищем/создаем согласование, в котором заявка является присоединяемая и проставляем ему статус.
        var approval = sharedRideApproveService.getByAddedRequestId(requestId).orElse(new SharedRideJoinApproval());
        log.debug("STEP 3: sharedRideApproval info {}", approval);
        boolean needToBeApproved = PERSONAL_AWAITING_SHARED_RIDE_APPROVAL == tripRequestStatus;
        log.debug("STEP 4: needToBeApproved {}, requestStatus: {}", needToBeApproved, tripRequestStatus);
        var status = getStatus(approval, message.isDeleted(), tripRequestStatus, needToBeApproved);
        log.debug("STEP 5: status {}", status);
        if (status == null) {
            logSkip(message.getId(), tripRequestStatus.name(), SHARED_RIDE_APPROVE_NAME, "status logic for added request");
            return;
        }
        approval.setStatus(status);
        if (isNew(approval)) {
            log.debug("STEP 6: message.rideId() = {}", message.getRideId());
            // Если согласование новое, то ищем trip request согласование для владельца личного транспорта, чтобы
            // заполнить по ней новое согласование
            if (message.getRideId() == null) {
                logSkip(requestId, tripRequestStatus.name(), SHARED_RIDE_APPROVE_NAME,
                        "rideId must be defined for request status");
                return;
            }
            var owner = approveTripRequestService.findSharedRideOwnerApprove(message.getRideId());
            if (owner.isEmpty()) {
                logSkip(requestId, tripRequestStatus.name(), SHARED_RIDE_APPROVE_NAME,
                        "Not found shared ride owner for shared ride " + message.getRideId());
                return;
            }
            log.debug("STEP 7: Copying owner {}, approval {}", owner.get(), approval);
            mapper.toBaseRequestApproval(message, approval);
            approval.setPassenger(passenger);
            approval.setApprovedById(owner.get().getActorId());
            approval.setAddRequestId(requestId);// присоединяемая поездка
        }
        log.debug("STEP 8: saving approval {}", approval);
        handleFraudData(message, approval);
        sharedRideApproveService.save(approval);
    }

    private void sendToApprovers(RequestMessage message, UUID actionId) {
        var approversIds = tripApproverService.computeApprovers(message.getPassengerId())
                .stream()
                .filter(approver -> approver.getTransportType() == null ||
                        message.getTransportType().equals(approver.getTransportType()))
                .map(Approver::getEmployeeId)
                .distinct()
                .toList();
        approveSender.send(actionId, approversIds);
    }

    /**
     * Общая для всех согласований логика определения статуса
     *
     * @param approval {@link BaseRequestApproval}
     * @param requestStatus {@link TripRequestStatus}
     * @return new status
     */
    private static Status getStatus(BaseRequestApproval approval,
                                   Boolean deleted,
                                   TripRequestStatus requestStatus,
                                   boolean needToBeApproved) {
        if (DECLINED.equals(approval.getStatus()) ||
                CANCELLED.equals(approval.getStatus()) ||
                APPROVED.equals(approval.getStatus())) {
            return approval.getStatus();
        }
        if (!isNew(approval) && (deleted || TripRequestStatus.getCanceledStatuses().contains(requestStatus))) {
            // Иначе если согласование существует и заявку отменили, ставим статус CANCELLED
            return CANCELLED;
        }
        if (needToBeApproved) {
            // Иначе, если заявка в статусе, когда нужно создавать согласование, ставим NEW или EDIT в зависимости от
            // существования заявки
            return isNew(approval) ? NEW : EDITED;
        }
        return null;
    }

    private static boolean isNew(Approval approval) {
        return approval.getId() == null;
    }

    private static void logSkip(UUID messageId, String status, String approveName, String reason) {
        log.info("Skip request message for {} approval}. Request id: {}, status: {}. Reason: {}",
                approveName, messageId, status, reason);
    }
}
