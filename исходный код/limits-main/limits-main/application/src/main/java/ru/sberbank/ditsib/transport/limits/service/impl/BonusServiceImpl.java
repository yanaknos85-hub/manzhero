package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.BonusRepository;
import ru.sberbank.ditsib.transport.limits.dao.BonusRequestRepository;
import ru.sberbank.ditsib.transport.limits.exceptions.BonusLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.EmployeeNotFoundException;
import ru.sberbank.ditsib.transport.limits.exceptions.ReservationFailedException;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.bonus.Bonus;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusOperation;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequest;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequestStatus;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class BonusServiceImpl implements BonusService {
    
    private final BonusRepository bonusRepository;
    private final BonusRequestRepository bonusRequestRepository;
    private final EmployeeService employeeService;
    private final LimitSharingService limitSharingService;
    private final LimitSharingPerPeriodService<Month> limitSharingPerPeriodService;
    private final DepLimitService depLimitService;
    private final DepartmentService departmentService;
    
    
    @Value("${bonus.defaultSum:2000}")
    private BigDecimal defaultSum;
    
    @Override
    public Bonus get(@NonNull UUID ownerId) {
        return bonusRepository.findById(ownerId)
                              .orElseGet(() -> (employeeService.get(ownerId)
                                                               .map(employee -> bonusRepository.save(Bonus.builder()
                                                                                                          .sum(defaultSum)
                                                                                                          .ownerId(ownerId)
                                                                                                          .requests(Collections.emptyList())
                                                                                                          .build())
                                                                   )
                                                               .orElseThrow(() -> new EmployeeNotFoundException(ownerId))));
    }
    
    @Override
    public BonusRequest deposit(
            @NonNull UUID ownerId,
            @NonNull BigDecimal sum,
            @NonNull String humanReadableId,
            @NonNull TransportTypeEnum transportType
                               ) {
        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            throw new BonusLogicException("The sum is less than 0. Sum: " + sum);
        }
        var bonus = get(ownerId);
        var reason = getReason(humanReadableId, transportType, BonusOperation.DEPOSIT);
        var employee = employeeService.get(bonus.getOwnerId()).orElseThrow(() -> new EmployeeNotFoundException(bonus.getOwnerId()));
        var depSharingLimit = getFromDepartmentLimit(employee, LocalDate.now().getYear(), transportType, LocalDateTime.now(),
                                                     sum);
        if (depSharingLimit == null) {
            throw new BonusLogicException("Department limit not found");
        }
        
        // Списываем средства с лимита подразделения
        changeSum(depSharingLimit.getLimitSharing(), depSharingLimit, sum);
        
        var request = BonusRequest.builder()
                                  .sum(sum)
                                  .operation(BonusOperation.DEPOSIT)
                                  .status(BonusRequestStatus.DONE)
                                  .transportType(transportType)
                                  .reason(getReason(humanReadableId, transportType, BonusOperation.DEPOSIT))
                                  .bonus(bonus)
                                  .build();
        bonusRequestRepository.save(request);
        // Начисляем их на бонусный счёт
        bonus.setBalance(bonus.getBalance().add(sum));
        bonusRepository.save(bonus);
        log.info("DEPOSITE: bonus deposit owner: {}, sum: {}, reason: {}", ownerId, sum, reason);
        
        return request;
    }
    
    @Override
    public BonusRequest reserve(
            @NonNull UUID ownerId,
            @NonNull BigDecimal sum,
            @NonNull UUID requestId,
            @NonNull String humanReadableId,
            @NonNull TransportTypeEnum transportType,
            boolean checkLimit
                               ) {
        var bonus = get(ownerId);
        var totalSum = bonus.getBalance().subtract(sum);
        if (totalSum.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReservationFailedException("The total sum is less than 0");
        }
        // При активном флаге делаем проверку нужно суммы на счету и возвращаем пустой ответ
        if (checkLimit) {
            return BonusRequest.builder().sum(sum).build();
        }
        bonus.setBalance(totalSum);
        var reason = getReason(humanReadableId, transportType, BonusOperation.SPEND);
        var request = BonusRequest.builder()
                                  .sum(sum)
                                  .operation(BonusOperation.SPEND)
                                  .status(BonusRequestStatus.RESERVED)
                                  .bonus(bonus)
                                  .requestId(requestId)
                                  .reason(getReason(humanReadableId, transportType, BonusOperation.SPEND))
                                  .transportType(transportType)
                                  .build();
        bonusRequestRepository.save(request);
        bonusRepository.save(bonus);
        
        log.info("RESERVE: bonus deposit owner: {}, requestId: {}, sum: {}, reason: {}", ownerId, requestId, sum, reason);
        
        return request;
    }
    
    @Override
    public void spend(@NonNull BonusRequest request) {
        var requestDB = bonusRequestRepository.findById(request.getId())
                                              .orElseThrow(() -> new BonusLogicException("Request with id %s now found".formatted(request.getId())));
        
        if (!requestDB.getStatus().equals(BonusRequestStatus.RESERVED)) {
            throw new BonusLogicException("Request is " + requestDB.getStatus());
        }
        var bonus = requestDB.getBonus();
        requestDB.setStatus(BonusRequestStatus.DONE);
        requestDB.setBonus(bonus);
        bonus.getRequests().add(requestDB);
        
        bonusRepository.save(bonus);
        bonusRequestRepository.save(requestDB);
        
        log.info("SPEND: bonus deposit requestId: {}", request.getRequestId());
    }
    
    @Override
    public void cancel(@NonNull BonusRequest request) {
        var requestDB = bonusRequestRepository.findById(request.getId())
                                              .orElseThrow(() -> new BonusLogicException("Request with id %s now found".formatted(request.getId())));
        
        if (!requestDB.getStatus().equals(BonusRequestStatus.RESERVED)) {
            throw new BonusLogicException("Request is " + requestDB.getStatus());
        }
        requestDB.setStatus(BonusRequestStatus.CANCELED);
        var bonus = requestDB.getBonus();
        bonus.setBalance(bonus.getBalance().add(requestDB.getSum()));
        
        bonusRepository.save(bonus);
        bonusRequestRepository.save(requestDB);
        
        log.info("SPEND: bonus deposit requestId: {}", request.getRequestId());
    }
    
    @Override
    public Optional<BonusRequest> getByRequestId(UUID requestId) {
        return bonusRequestRepository.findByRequestId(requestId);
    }
    
    private String getReason(String humanReadableId, TransportTypeEnum transportType, BonusOperation operation) {
        var reason = "";
        if (operation.equals(BonusOperation.SPEND)) {
            reason = "Списание за повышение класса %s по заявке № %s";
        } else {
            reason = "Пополнение за счет совместной поездки на %s по заявке № %s";
        }
        
        var transportRusName = resolveTransportType(transportType);
        
        return reason.formatted(transportRusName, humanReadableId);
    }
    
    private String resolveTransportType(TransportTypeEnum transportType) {
        if (!(transportType.equals(TransportTypeEnum.TAXI) || transportType.equals(TransportTypeEnum.PERSONAL))) {
            throw new BonusLogicException("Unexpected transport type " + transportType);
        }
        if (transportType.equals(TransportTypeEnum.PERSONAL)) {
            return "личном транспорте";
        }
        return transportType.getRusName().toLowerCase();
    }
    
    /**
     * Change sum on limit.
     *
     * @param limitSharing limitSharing
     * @param limitSharingPerPeriod limitSharingPerPeriod.
     * @param sum sum.
     */
    private void changeSum(LimitSharing limitSharing, LimitSharingPerPeriod limitSharingPerPeriod, BigDecimal sum) {
        limitSharing.setBalance(limitSharing.getBalance().subtract(sum));
        limitSharingPerPeriod.setBalance(limitSharingPerPeriod.getBalance().subtract(sum));
        limitSharingService.save(limitSharing);
        limitSharingPerPeriodService.save(limitSharingPerPeriod);
    }
    
    /**
     * Проверка на наличие лимита подразделения.
     *
     * @param employee employee
     * @param year year
     * @param transportType transport type.
     * @param plannedDate planned date.
     * @param sum sum.
     *
     * @return limit sharing object.
     */
    private LimitSharingPerPeriod getFromDepartmentLimit(
            Employee employee, int year,
            TransportTypeEnum transportType, LocalDateTime plannedDate,
            BigDecimal sum
                                                       ) {
        DepLimit depLimit = getDepartmentLimit(employee.getDepartmentId(), year, getLimitServiceTypeByTransportType(transportType).name());
        if (depLimit == null) {
            log.info("Бонусный счёт: лимит подразделения не найден для сотрудника " + employee.getId()
                     + " на вид транспорта " + transportType
                     + " на сумму " + sum
                     + " на дату " + plannedDate);
            return null;
        }
        if (!depLimit.getDepartment().getId().equals(employee.getDepartmentId()) && !depLimit.isUseThisLimit()) {
            log.info("Бонусный счёт: лимит подразделения в иерархии найден без признака 'использовать лимит моего подразделения'"
                     + " для сотрудника " + employee.getId()
                     + " на вид транспорта " + transportType
                     + " на сумму " + sum
                     + " на дату " + plannedDate);
            return null;
        }
        return getFromLimit(employee, depLimit, transportType, plannedDate, sum);
    }
    
    private DepLimit getDepartmentLimit(UUID departmentId, int year, String limitServiceType) {
        DepLimit limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentId, year, limitServiceType);
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
     * @param limit limit
     * @param transportType transport type.
     * @param plannedDate planned date.
     * @param sum sum.
     *
     * @return limit sharing object.
     */
    private LimitSharingPerPeriod getFromLimit(
            Employee employee, Limit limit,
            TransportTypeEnum transportType, LocalDateTime plannedDate,
            BigDecimal sum
                                             ) {
        if (limit.getLimitStatus() != LimitStatus.SHARED) {
            log.info("Бонусный счёт: лимит не распределен для сотрудника %s лимит %s тип лимита %s статус лимита %s"
                             .formatted(employee.getId(), limit.getId(), limit.getLimitType(), limit.getLimitStatus()));
            return null;
        }
        final var limitSharing = limitSharingService.getByLimitAndTransportType(limit, transportType);
        if (limitSharing == null) {
            log.info("Бонусный счёт: лимит не выделен для сотрудника %s лимит %s тип лимита %s на вид транспорта %s"
                             .formatted(employee.getId(), limit.getId(), limit.getLimitType(), transportType));
            return null;
        }
        if (limitSharing.getBalance().compareTo(sum) < 0) {
            log.info("Бонусный счёт: лимит не достаточен для сотрудника %s лимит %s тип лимита %s на вид транспорта %s на сумму %s"
                             .formatted(employee.getId(), limit.getId(), limit.getLimitType(), transportType, sum));
            return null;
        }
        //get current period and check balance
        var limitSharingPerPeriod = limitSharingPerPeriodService.getForDate(limitSharing, plannedDate.toLocalDate());
        if (limitSharingPerPeriod == null) {
            log.info("Бонусный счёт: лимит в периоде не найден для сотрудника %s лимит %s тип лимита %s на вид транспорта %s на дату %s".formatted(
                    employee.getId(), limit.getId(), limit.getLimitType(), transportType, plannedDate));
            return null;
        }
        if (limitSharingPerPeriod.getBalance().compareTo(sum) < 0) {
            log.info(
                    "Бонусный счёт: лимит в периоде не достаточен для сотрудника %s лимит %s тип лимита %s на вид транспорта %s на сумму %d на период %s".formatted(
                            employee.getId(), limit.getId(), limit.getLimitType(), transportType, sum, limitSharingPerPeriod.getPeriod()));
            return null;
        }
        return limitSharingPerPeriod;
    }
    
}
