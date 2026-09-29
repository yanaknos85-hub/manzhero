package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.limits.business.LimitSharingPerPeriods;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.exceptions.BonusLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitNotSufficientException;
import ru.sberbank.ditsib.transport.limits.exceptions.ReservationFailedException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequest;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;


/**
 * Implementation of service for working with organization.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class ReservationServiceImpl implements ReservationService {

    private final LimitSpendingService limitSpendingService;

    private final Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    private final EmpLimitService empLimitService;

    private final DepLimitService depLimitService;

    private final LimitService limitService;

    private final LimitSharingService limitSharingService;

    private final DepartmentService departmentService;

    private final BonusService bonusService;

    private final LimitHistoryService limitHistoryService;

    private final LimitSharingPerPeriods limitSharingPerPeriods;

    @Override
    @Transactional
    public LimitSpending reserve(
            Employee employee, String humanReadableId,
            LocalDateTime plannedDate, TransportTypeEnum transportType,
            BigDecimal sum, UUID requestId, BigDecimal bonusSum, boolean checkLimit
    ) {
        return reserve(employee, humanReadableId, plannedDate, transportType, sum, requestId, null, bonusSum, checkLimit);
    }

    @Override
    @Transactional
    public LimitSpending rereserve(
            Employee employee, String humanReadableId,
            LocalDateTime plannedDate, TransportTypeEnum transportType,
            BigDecimal sum, LimitSpending limitSpendingOld
    ) {
        // cancel old reservation, delete old limitSpending object, make new reservation
        if (!limitSpendingOld.getStatus().equals(LimitSpendingStatus.RESERVED)) {
            throw new ReservationFailedException("Незвозможно перерезервировать: резервирование в статусе "
                    + limitSpendingOld.getStatus());
        }
        boolean rereservationPossible = isRereservationPossible(limitSpendingOld, employee, plannedDate,
                transportType, sum);
        if (!rereservationPossible) {
            throw new LimitNotSufficientException(limitSpendingOld.getLimitSharingPerPeriod().getLimitSharing()
                    .getLimit().getHumanReadableId(),
                    transportType);
        }
        UUID requestId = limitSpendingOld.getRequestId();

        final var bonusSum = isSpendingFromBonusAccountAllowed(transportType)
                ? bonusService.getByRequestId(limitSpendingOld.getRequestId()).map(BonusRequest::getSum).orElse(null)
                : null;
        return reserve(employee, humanReadableId, plannedDate, transportType, sum, requestId, limitSpendingOld, bonusSum, false);
    }

    @Override
    @Transactional
    public LimitSpending cancelReserve(LimitSpending limitSpending) {
        UUID authorId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        limitSpending = limitSpendingService.get(limitSpending.getId()).orElseThrow();
        var bonusRequest = bonusService.getByRequestId(limitSpending.getRequestId());
        log.info("Отмена резервирования: вызвана для сотрудника %s на сумму %s на дату %s".formatted(limitSpending.getEmployee().getId(),
                limitSpending.getSumReserved(),
                limitSpending.getReservationTime()));
        var limitSharingPerPeriod = limitSpending.getLimitSharingPerPeriod();
        var limitSharing = limitSharingPerPeriod.getLimitSharing();

        var sourceLimit = limitSharing.getLimit();
        var sharingType = sourceLimit.getLimitSharingType();
        changeSum(limitSharing,
                limitSharingPerPeriod,
                limitSpending.getSumReserved().negate(),
                bonusRequest.isPresent() && bonusRequest.get().getSum() != null ? bonusRequest.get().getSum().negate() : BigDecimal.ZERO,
                sharingType
        );
        limitSpending.setCancelTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitSpending.setStatus(LimitSpendingStatus.CANCELED);
        bonusRequest.ifPresent(bonusService::cancel);
        limitSpendingService.save(limitSpending);

        if (sourceLimit.getLimitStatus().equals(LimitStatus.CLOSED)) {
            var targetLimit = findUpperActiveLimit(sourceLimit);
            var targetLimitSharing = limitSharingService.getByLimitAndTransportType(targetLimit,
                    limitSharing
                            .getTransportType());
            if (targetLimitSharing != null) {
                var source = new LimitData(sourceLimit, targetLimitSharing.getTransportType());
                var target = new LimitData(targetLimit, targetLimitSharing.getTransportType());
                limitService.transferSum(source, target, limitSpending.getSumReserved(), authorId, true);
            }
        }

        limitHistoryService.add(authorId,
                new LimitData(sourceLimit, limitSharing.getTransportType(), limitSharingPerPeriod.getPeriod()),
                null,
                limitSpending.getSumReserved(), sourceLimit.getYear(),
                LimitHistoryType.CANCEL_RESERVE,
                limitSharing,
                limitSharingPerPeriod);
        return limitSpending;
    }

    private Limit findUpperActiveLimit(Limit limit) {
        var parentLimit = limit;
        while (parentLimit.getLimitStatus().equals(LimitStatus.CLOSED)) {
            parentLimit = parentLimit.getParent();
        }
        return parentLimit;
    }

    @Override
    @Transactional
    public LimitSpending spend(LimitSpending limitSpending, BigDecimal sumSpent) {
        var bonusRequest = bonusService.getByRequestId(limitSpending.getRequestId());
        var bonusSum = bonusRequest.map(BonusRequest::getSum).orElse(null);
        log.info("Подтверждение резервирования: вызвано для сотрудника %s на сумму резервирования %s на сумму фактическую %s%s на дату %s".formatted(
                limitSpending.getEmployee().getId(), limitSpending.getSumReserved(), sumSpent,
                bonusRequest.map(request -> (" списано бонусов " + request.getSum())).orElse(""), limitSpending.getReservationTime()));

        limitSpending.setSpendingTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitSpending.setSumSpent(sumSpent);
        limitSpending.setStatus(LimitSpendingStatus.SPENT);
        validateRequestSums(sumSpent, bonusSum);
        bonusRequest.ifPresent(bonusService::spend);
        limitSpendingService.save(limitSpending);
        return limitSpending;
    }

    /**
     * Actual reservation.
     *
     * @param employee         employee
     * @param plannedDate      planned date
     * @param transportType    transport type.
     * @param sum              sum.
     * @param requestId        request id.
     * @param limitSpendingOld old limit spending.
     * @param checkLimit       change limit or just check
     * @return limit spending object.
     */
    private LimitSpending reserve(
            Employee employee, String humanReadableId,
            LocalDateTime plannedDate, TransportTypeEnum transportType,
            BigDecimal sum, UUID requestId, LimitSpending limitSpendingOld, BigDecimal bonusSum, boolean checkLimit
    ) {
        if (sum.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reserve: sum must be grater then 0");
        }
        validateRequestSums(sum, bonusSum);
        BonusRequest bonusRequest = null;
        if (isSpendingFromBonusAccountAllowed(transportType) && bonusSum != null && bonusSum.compareTo(BigDecimal.ZERO) > 0) {
            bonusRequest = bonusService.reserve(employee.getId(), bonusSum, requestId, humanReadableId, transportType, checkLimit);
        }
        log.info("Резервирование: вызвано для сотрудника %s на вид транспорта %s на сумму %s на дату %s".formatted(employee.getId(), transportType,
                sum, plannedDate));
        var limitSharingPerPeriod = getFromEmployeeLimit(employee, plannedDate.getYear(), transportType, plannedDate, sum);
        if (limitSharingPerPeriod == null) {
            limitSharingPerPeriod = getFromDepartmentLimit(employee, plannedDate.getYear(), transportType, plannedDate, sum);
        }
        if (limitSharingPerPeriod == null) {
            String str = String.format(
                    "Резервирование: не возможно для сотрудника %s на вид транспорта %s на сумму %s на дату %s",
                    employee.getId(), transportType, sum.stripTrailingZeros().toPlainString(), plannedDate);
            log.info(str);
            throw new ReservationFailedException(str);
        }
        var limitSharing = limitSharingPerPeriod.getLimitSharing();
        var limit = limitSharing.getLimit();
        var period = limitSharingPerPeriod.getPeriod();

        var limitSpending = Optional.ofNullable(limitSpendingOld).map(LimitSpending::getRequestId)
            .map(limitSpendingService::getByRequestId).orElseGet(LimitSpending::new);
        limitSpending.setEmployee(employee);
        limitSpending.setReservationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitSpending.setStatus(LimitSpendingStatus.RESERVED);
        limitSpending.setSumReserved(sum.add(bonusRequest != null ? bonusRequest.getSum() : BigDecimal.ZERO));
        limitSpending.setLimitSharingPerPeriod(limitSharingPerPeriod);
        limitSpending.setRequestId(requestId);

        // При активном флаге делаем проверку нужной суммы на счету без сохранения в бд
        if (checkLimit) {
            return limitSpending;
        }

        changeSum(limitSharing, limitSharingPerPeriod, sum, bonusSum == null ? BigDecimal.ZERO : bonusSum,
                limit.getLimitSharingType());

        if (limitSpendingOld != null) {
            bonusService.getByRequestId(requestId).ifPresent(bonusService::cancel);
        }
        limitSpendingService.save(limitSpending);

        limitHistoryService.add(employee.getId(),
                new LimitData(limit, limitSharing.getTransportType(), period),
                null,
                sum.negate(), plannedDate.getYear(),
                LimitHistoryType.RESERVE,
                limitSharing,
                limitSharingPerPeriod);

        return limitSpending;
    }

    /**
     * Проверка на наличие личного лимита.
     *
     * @param employee      employee
     * @param year          year
     * @param transportType transport type.
     * @param plannedDate   planned date.
     * @param sum           sum.
     * @return limit sharing object.
     */
    private LimitSharingPerPeriod getFromEmployeeLimit(
            Employee employee, int year,
            TransportTypeEnum transportType, LocalDateTime plannedDate,
            BigDecimal sum
    ) {
        Limit limit = empLimitService.getByEmployeeAndYearAndLimitServiceType(employee, year, getLimitServiceTypeByTransportType(transportType).name());
        if (limit == null) {
            log.info(
                    "Резервирование: личный лимит не найден для сотрудника %s на вид транспорта %s на сумму %s на дату %s"
                            .formatted(employee.getId(), transportType, sum, plannedDate));
            return null;
        }
        return getFromLimit(employee, limit, transportType, plannedDate, sum);
    }

    /**
     * Проверка на наличие лимита подразделения.
     *
     * @param employee      employee
     * @param year          year
     * @param transportType transport type.
     * @param plannedDate   planned date.
     * @param sum           sum.
     * @return limit sharing object.
     */
    private LimitSharingPerPeriod getFromDepartmentLimit(
            Employee employee, int year,
            TransportTypeEnum transportType, LocalDateTime plannedDate,
            BigDecimal sum
    ) {
        var depLimit = getDepartmentLimit(employee.getDepartmentId(), year, getLimitServiceTypeByTransportType(transportType).name());
        if (depLimit == null) {
            log.info("Резервирование: лимит подразделения не найден для сотрудника %s на вид транспорта %s на сумму %s на дату %s".formatted(
                    employee.getId(), transportType, sum, plannedDate));
            return null;
        }
        if (!depLimit.getDepartment().getId().equals(employee.getDepartmentId()) && !depLimit.isUseThisLimit()) {
            log.info(
                    "Резервирование: лимит подразделения в иерархии найден без признака 'использовать лимит моего подразделения' для сотрудника %s на вид транспорта %s на сумму %s на дату %s".formatted(
                            employee.getId(), transportType, sum, plannedDate));
            return null;
        }
        return getFromLimit(employee, depLimit, transportType, plannedDate, sum);
    }

    private DepLimit getDepartmentLimit(UUID departmentId, int year, String limitServiceType) {
        var limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentId, year, limitServiceType, false);
        if (limit != null) {
            return limit;
        } else {
            Department department = departmentService.get(departmentId).orElse(null);
            if (department == null) {
                log.error("ERROR: ReservationService: department not found for id " + departmentId);
                return null;
            }
            if (department.getParentId() != null) {
                return getDepartmentLimit(department.getParentId(), year, limitServiceType);
            } else {
                return null;
            }
        }
    }

    /**
     * Проверка на наличие лимита подразделения.
     *
     * @param limit         limit
     * @param transportType transport type.
     * @param plannedDate   planned date.
     * @param sum           sum.
     * @return limit sharing object.
     */
    private LimitSharingPerPeriod getFromLimit(
            Employee employee, Limit limit,
            TransportTypeEnum transportType, LocalDateTime plannedDate,
            BigDecimal sum
    ) {
        if (limit.getLimitStatus() != LimitStatus.SHARED) {
            log.info("Резервирование: лимит не распределен для сотрудника %s лимит %s тип лимита %s статус лимита %s"
                    .formatted(employee.getId(), limit.getId(), limit.getLimitType(), limit.getLimitStatus()));
            return null;
        }
        var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(limit.getLimitSharingType());
        LimitSharing limitSharing = limitSharingService.getByLimitAndTransportType(limit, transportType);
        if (limitSharing == null) {
            log.info("Резервирование: лимит не выделен для сотрудника %s лимит %s тип лимита %s на вид транспорта %s".formatted(employee.getId(),
                    limit.getId(),
                    limit.getLimitType(),
                    transportType));
            return null;
        }
        if (limitSharing.getBalance().compareTo(sum) < 0) {
            log.info("Резервирование: лимит не достаточен для сотрудника %s лимит %s тип лимита %s на вид транспорта %s на сумму %s".formatted(
                    employee.getId(), limit.getId(), limit.getLimitType(), transportType, sum));
            return null;
        }
        //get current period and check balance
        var limitSharingPerPeriod =
                limitSharingPerPeriodService.getForDate(limitSharing, plannedDate.toLocalDate());
        if (limitSharingPerPeriod == null) {
            log.info("Резервирование: лимит в периоде не найден для сотрудника {} лимит {} тип лимита {} на вид транспорта {} на дату {}",
                    employee.getId(), limit.getId(), limit.getLimitType(), transportType, plannedDate);
            return null;
        }
        if (limitSharingPerPeriod.getBalance().compareTo(sum) < 0) {
            log.info("Резервирование: лимит в периоде не достаточен для сотрудника {} лимит {} тип лимита {} на вид транспорта {} на сумму {} на период {}",
                    employee.getId(), limit.getId(), limit.getLimitType(), transportType, sum, limitSharingPerPeriod.getPeriod().name());
            return null;
        }
        return limitSharingPerPeriod;
    }

    /**
     * Check if reservation possible.
     *
     * @param limitSpending limitSpending
     * @param employee      employee
     * @param plannedDate   planned date
     * @param transportType transport type
     * @param sumNew        new sum.
     * @return true if reservation possible, false otherwise.
     */
    private boolean isRereservationPossible(
            LimitSpending limitSpending, Employee employee, LocalDateTime plannedDate,
            TransportTypeEnum transportType, BigDecimal sumNew
    ) {
        limitSpending = limitSpendingService.get(limitSpending.getId()).orElseThrow();
        Limit limitOld = limitSpending.getLimitSharingPerPeriod().getLimitSharing().getLimit();
        Period plannedPeriod = getPeriodForDate(limitOld, plannedDate);
        if (plannedDate.getYear() == limitOld.getYear() &&
                transportType == limitSpending.getLimitSharingPerPeriod().getLimitSharing().getTransportType() &&
                plannedPeriod == limitSpending.getLimitSharingPerPeriod().getPeriod()) {
            var balanceLimitSharing = limitSpending.getLimitSharingPerPeriod().getLimitSharing().getBalance();
            var balanceLimitSharingPerPeriod = limitSpending.getLimitSharingPerPeriod().getBalance();

            balanceLimitSharing = balanceLimitSharing.add(limitSpending.getSumReserved());
            balanceLimitSharingPerPeriod = balanceLimitSharingPerPeriod.add(limitSpending.getSumReserved());

            return (balanceLimitSharing.compareTo(sumNew) >= 0) && (balanceLimitSharingPerPeriod.compareTo(sumNew) >= 0);
        } else { // different year / transport type / period
            Limit limit = empLimitService.getByEmployeeAndYearAndLimitServiceType(employee, plannedDate.getYear(),
                    getLimitServiceTypeByTransportType(transportType).name());
            if (limit == null) {
                limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(employee.getDepartmentId(), plannedDate.getYear(),
                        getLimitServiceTypeByTransportType(transportType).name());
            }
            if (limit == null) {
                return false;
            }
            var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(limit.getLimitSharingType());
            var limitSharing = limitSharingService.getByLimitAndTransportType(limit, transportType);
            if (limitSharing == null) {
                return false;
            }
            //get current period and check balance
            var limitSharingPerPeriod = limitSharingPerPeriodService.getForDate(limitSharing, plannedDate.toLocalDate());
            return (limitSharing.getBalance().compareTo(sumNew) >= 0) && (limitSharingPerPeriod.getBalance().compareTo(sumNew) >= 0);
        }
    }

    /**
     * Change sum on limit.
     *
     * @param limitSharing          limitSharing
     * @param limitSharingPerPeriod limitSharingPerPeriod.
     * @param sum                   sum.
     */
    private void changeSum(
            LimitSharing limitSharing, LimitSharingPerPeriod limitSharingPerPeriod, BigDecimal sum, BigDecimal bonusSum,
            LimitSharingType limitSharingType
    ) {
        var subtractedBonus = sum.subtract(bonusSum);
        limitSharing.setBalance(limitSharing.getBalance().subtract(subtractedBonus));
        limitSharingPerPeriod.setBalance(limitSharingPerPeriod.getBalance().subtract(subtractedBonus));
        limitSharingService.save(limitSharing);
        limitSharingPerPeriodServices.get(limitSharingType).save(limitSharingPerPeriod);

        limitSharingPerPeriods.checkRemains(limitSharingPerPeriod.getId());
    }

    /**
     * Gets period for date.
     *
     * @param limit       limit
     * @param plannedDate planned date.
     * @return period number.
     */
    private Period getPeriodForDate(Limit limit, LocalDateTime plannedDate) {
        if (limit.getLimitSharingType() == LimitSharingType.MONTHLY
                || limit.getLimitSharingType() == LimitSharingType.PERCENTS) {
            return Month.valueOf(plannedDate.getMonth());
        } else if (limit.getLimitSharingType() == LimitSharingType.QUARTER) {
            return Quarter.valueOf(plannedDate.getMonthValue() / 3);
        }
        throw new UnsupportedOperationException("Limit sharing type %s is not supported".formatted(limit.getLimitSharingType()));
    }

    private void validateRequestSums(BigDecimal sum, BigDecimal bonusSum) {
        if (bonusSum != null && bonusSum.compareTo(sum) > 0) {
            throw new BonusLogicException("Bonus cost cannot be greater than cost");
        }
    }

    private boolean isSpendingFromBonusAccountAllowed(TransportTypeEnum transportType) {
        return transportType == TransportTypeEnum.TAXI;
    }
}
