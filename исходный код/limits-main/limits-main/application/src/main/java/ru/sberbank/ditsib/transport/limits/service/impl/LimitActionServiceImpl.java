package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.limits.ReservationStrategy;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.exceptions.ActionNotAuthorizedException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitNotSufficientException;
import ru.sberbank.ditsib.transport.limits.exceptions.ReservationFailedException;
import ru.sberbank.ditsib.transport.limits.model.LimitReservationStatus;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.dto.limits.ReservationStrategy.ALL;
import static ru.sberbank.ditsib.transport.limits.model.LimitReservationStatus.*;


/**
 * Implementation of service for working with limits reservation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LimitActionServiceImpl implements LimitActionService {

    private final EmployeeService employeeService;

    private final DepartmentService departmentService;

    private final LimitSpendingService limitSpendingService;

    private final ReservationService reservationService;

    private final BonusService bonusService;

    // Кол-во процентов от суммы поездки в качестве бонусов за совместную поездку
    @Value("${bonus.cashback.percents:0}")
    private long cashbackPercents;

    @Transactional
    @Override
    public LimitActionResultMessage processLimitAction(LimitActionMessage message) {
        try {
            if ("RESERVE".equals(message.action())) {
                return processReservation(message);
            } else if ("CANCEL".equals(message.action())) {
                return processCancel(message.requestId());
            } else if ("SPEND".equals(message.action())) {
                return processSpend(message);
            }
        } catch (EntityNotFoundException e) {
            return getAnswer(ERROR_EMPLOYEE_NOT_FOUND, e.getMessage(), message.requestId());
        } catch (ActionNotAuthorizedException e) {
            return getAnswer(ERROR_NOT_AUTHORIZED, e.getMessage(), message.requestId());
        } catch (ReservationFailedException e) {
            return getAnswer(LIMIT_NOT_FOUND, e.getMessage(), message.requestId());
        } catch (LimitNotSufficientException e) {
            return getAnswer(LIMIT_NOT_SUFFICIENT, e.getMessage(), message.requestId());
        } catch (IllegalArgumentException e) {
            return getAnswer(SUM_INCORRECT, e.getMessage(), message.requestId());
        }
        return getAnswer(ACTION_TYPE_ERROR, String.format("Action type '%s' not found",
                message.action()), message.requestId());
    }

    @Override
    public MassLimitActionResultMessage processMassLimitAction(
            UUID userId,
            List<LimitActionMessage> messages,
            ReservationStrategy reservationStrategy,
            String action
    ) {
        try {
            if ("RESERVE".equals(action)) {
                if (userId == null) {
                    return getMassAnswer(ERROR_NOT_AUTHORIZED, "User not authorized");
                }

                var results = processReservation(messages, reservationStrategy);

                return getMassAnswer(LIMIT_RESERVED, "Success", results);
            }
        } catch (Exception e) {
            log.warn("Mass limit reservation failed. {}", e.getMessage());
            if (e instanceof ReservationFailedException) {
                return getMassAnswer(LIMIT_RESERVATION_FAILED, e.getMessage());
            } else if (e instanceof EntityNotFoundException) {
                return getMassAnswer(ERROR_EMPLOYEE_NOT_FOUND, e.getMessage());
            }
        }

        return getMassAnswer(ACTION_TYPE_ERROR, String.format("Action type '%s' not found", action));
    }

    @Override
    public MassLimitActionResultMessage processMassLimitAction(UUID userId, MassLimitActionMessage massLimitActionMessage) {
        return processMassLimitAction(userId,
                massLimitActionMessage.actionMessages(),
                ReservationStrategy.valueOf(massLimitActionMessage.reservationStrategy()),
                massLimitActionMessage.action());
    }

    public LimitActionResultMessage processReservation(
            LimitActionMessage message
    ) {
        var employee = employeeService.get(message.employeeId())
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, message.employeeId()));

        return processReservation(employee, message);
    }

    public List<LimitActionResultMessage> processReservation(
            List<LimitActionMessage> messages,
            ReservationStrategy reservationStrategy
    ) {
        List<LimitActionResultMessage> resultMessages = new ArrayList<>();

        var employees = employeeService.getAllById(messages.stream().map(LimitActionMessage::employeeId).toList()).stream().collect(
                Collectors.toMap(Employee::getId, Function.identity()));

        for (var message : messages) {
            try {
                resultMessages.add(processReservation(employees.get(message.employeeId()), message));
            } catch (Exception e) {
                log.warn("Mass reservation failed", e);
                var errorResult = getAnswerForException(e, message.requestId());
                resultMessages.add(errorResult);

                if (ALL.equals(reservationStrategy)) {
                    resultMessages.stream()
                            .map(LimitActionResultMessage::tripRequestId)
                            .filter(tripRequestId -> !tripRequestId.equals(message.requestId()))
                            .forEach(this::processCancel);
                    throw new ReservationFailedException("Ошибка при массовом резервировании лимита: " + errorResult.message());
                }
            }
        }

        return resultMessages;
    }

    private LimitActionResultMessage processReservation(Employee employee, LimitActionMessage message) {
        if (employee == null) {
            throw new EntityNotFoundException(Employee.class, message.employeeId());
        }
        if (!employee.getDepartmentId().equals(message.departmentId())) {
            throw new ActionNotAuthorizedException("Given employee does not belong to given department");
        }
        if (!departmentService.get(employee.getDepartmentId()).map(Department::getOrganizationId)
                .orElseThrow(IllegalArgumentException::new)
                .equals(message.organizationId())) {
            throw new ActionNotAuthorizedException("Given employee does not belong to given organization");
        }
        boolean checkLimit = Optional.ofNullable(message.checkLimit()).orElse(false);
        LimitSpending limitSpendingOld = null;
        if (!checkLimit) {
            limitSpendingOld = limitSpendingService.getByRequestId(message.requestId());
        }
        if (limitSpendingOld != null) {
            return handleAnswer(reservationService.rereserve(employee, message.humanReadableId(), message.plannedDate(),
                    TransportTypeEnum.valueOf(message.transportType()),
                    BigDecimal.valueOf(message.sum()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN), limitSpendingOld), message.requestId());
        } else {
            return handleAnswer(reservationService.reserve(employee, message.humanReadableId(), message.plannedDate(),
                            TransportTypeEnum.valueOf(message.transportType()),
                            BigDecimal.valueOf(message.sum()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN),
                            message.requestId(),
                            Optional.ofNullable(message.bonusSum()).map(BigDecimal::valueOf).map(it -> it.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)).orElse(BigDecimal.ZERO),
                            checkLimit),
                    message.requestId());
        }
    }

    private LimitActionResultMessage processCancel(UUID requestId) {
        var limitSpending = limitSpendingService.getByRequestId(requestId);
        var limitReservationStatus = LIMIT_CANCELLED;
        var message = "";
        UUID limitId = null;
        if (limitSpending == null) {
            limitReservationStatus = LIMIT_CANCEL_FAILED;
            message = "Проблема при отмене резервирования: резервирование не найдено для заявки на поездку " + requestId;
        } else if (limitSpending.getStatus().equals(LimitSpendingStatus.SPENT)) {
            limitId = limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId();
            limitReservationStatus = LIMIT_CANCEL_FAILED;
            message = "Проблема при отмене резервирования: резервирование в статусе " + limitSpending.getStatus();
        } else if (limitSpending.getStatus().equals(LimitSpendingStatus.RESERVED)) {
            limitId = limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId();
            reservationService.cancelReserve(limitSpending);
        }
        return getAnswer(limitReservationStatus, message, requestId, limitId);
    }

    public LimitActionResultMessage processSpend(LimitActionMessage spendMessage) {
        // get LimitSpending object
        var requestId = spendMessage.requestId();
        var limitSpending = limitSpendingService.getByRequestId(requestId);
        var limitReservationStatus = LIMIT_SPENT;
        var message = "";
        UUID limitId = null;
        if (limitSpending == null) {
            limitReservationStatus = LIMIT_SPENT_FAILED;
            message = "Проблема при фиксировании затрат: резервирование не найдено для заявки на поездку " + requestId;
        } else if (limitSpending.getStatus().equals(LimitSpendingStatus.CANCELED)) {
            limitId = limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId();
            limitReservationStatus = LimitReservationStatus.LIMIT_SPENT_FAILED;
            message = "Проблема при фиксировании затрат: резервирование в статусе " + limitSpending.getStatus();
        } else if (limitSpending.getStatus().equals(LimitSpendingStatus.RESERVED)) {
            limitId = limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId();
            reservationService.spend(limitSpending, BigDecimal.valueOf(spendMessage.sum()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN));
            depositBonus(spendMessage, limitSpending);
        }
        return getAnswer(limitReservationStatus, message, requestId, limitId);
    }

    private LimitActionResultMessage handleAnswer(LimitSpending limitSpending, UUID tripRequestId) {
        var limitReservationStatus = RESERVED_FROM_EMPLOYEE;
        var message = "";
        if (limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getLimitType() ==
                LimitType.DEPARTMENT) {
            limitReservationStatus = RESERVED_FROM_DEPARTMENT;
        }
        return getAnswer(limitReservationStatus, message, tripRequestId,
                limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId());
    }

    private LimitActionResultMessage getAnswer(
            LimitReservationStatus limitReservationStatus,
            String message,
            UUID tripRequestId
    ) {
        return new LimitActionResultMessage(tripRequestId, null, message, null, limitReservationStatus.name());
    }

    private LimitActionResultMessage getAnswer(
            LimitReservationStatus limitReservationStatus,
            String message,
            UUID tripRequestId,
            UUID limitId
    ) {
        return new LimitActionResultMessage(tripRequestId, limitId, message, null, limitReservationStatus.name());
    }

    private void depositBonus(LimitActionMessage spendMessage, LimitSpending limitSpending) {
        var transportType = TransportTypeEnum.valueOf(spendMessage.transportType());
        if (spendMessage.coop() && spendMessage.moneySaved() != null &&
                (TransportTypeEnum.TAXI.equals(transportType) ||
                        //Начисляем бонусы только пассажирам
                        (TransportTypeEnum.PERSONAL.equals(transportType) && !spendMessage.driver())) &&
                cashbackPercents > 0) {
            final var sum = BigDecimal.valueOf(spendMessage.moneySaved())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN) // копейки в рубли
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(cashbackPercents));
            bonusService.deposit(limitSpending.getEmployee().getId(),
                    sum,
                    spendMessage.humanReadableId(),
                    transportType
            );
        }
    }

    private MassLimitActionResultMessage getMassAnswer(
            LimitReservationStatus limitReservationStatus,
            String message,
            List<LimitActionResultMessage> resultMessages
    ) {
        return new MassLimitActionResultMessage(resultMessages, limitReservationStatus.name(), message);
    }

    private MassLimitActionResultMessage getMassAnswer(
            LimitReservationStatus limitReservationStatus,
            String message
    ) {
        return getMassAnswer(limitReservationStatus, message, Collections.emptyList());
    }

    private LimitActionResultMessage getAnswerForException(Exception e, UUID requestId) {
        if (e instanceof EntityNotFoundException) {
            return getAnswer(ERROR_EMPLOYEE_NOT_FOUND, e.getMessage(), requestId);
        }
        if (e instanceof ActionNotAuthorizedException) {
            return getAnswer(ERROR_NOT_AUTHORIZED, e.getMessage(), requestId);
        }
        if (e instanceof ReservationFailedException) {
            return getAnswer(LIMIT_NOT_FOUND, e.getMessage(), requestId);
        }
        if (e instanceof LimitNotSufficientException) {
            return getAnswer(LIMIT_NOT_SUFFICIENT, e.getMessage(), requestId);
        }

        return getAnswer(LIMIT_RESERVATION_FAILED, e.getMessage(), requestId);
    }
}
