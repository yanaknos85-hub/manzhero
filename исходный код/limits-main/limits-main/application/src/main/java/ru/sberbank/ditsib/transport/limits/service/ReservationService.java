package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for working with organizations.
 */
public interface ReservationService {
    
    /**
     * Reserve sum on limit. For taxi requests, spend bonuses from bonus account. Sum is a FULL AMOUNT of request (excluding bonuses).
     * Reserved sum from limit = sum - bonusSum
     *
     * @param employee employee
     * @param plannedDate planned date
     * @param transportType transport type.
     * @param sum sum.
     * @param requestId id of ride request.
     * @param checkLimit flag to change limit (false) or not
     *
     * @return limit spending object.
     */
    LimitSpending reserve(
            Employee employee, String humanReadableId, LocalDateTime plannedDate,
            TransportTypeEnum transportType, BigDecimal sum, UUID requestId, BigDecimal bonusSum, boolean checkLimit
                         );
    
    /**
     * Rereserve sum on limit. If bonus request linked with request exists, bonuses from bonus request will be used in new request
     *
     * @param employee employee
     * @param plannedDate planned date
     * @param transportType transport type.
     * @param sum sum.
     * @param limitSpendingOld old limit spending object.
     *
     * @return limit spending object.
     */
    LimitSpending rereserve(
            Employee employee, String humanReadableId, LocalDateTime plannedDate,
            TransportTypeEnum transportType, BigDecimal sum, LimitSpending limitSpendingOld
                           );
    
    /**
     * Cancel reservation.
     *
     * @param limitSpending limit spending.
     *
     * @return limit spending object.
     */
    LimitSpending cancelReserve(LimitSpending limitSpending);
    
    /**
     * Confirm reservation.
     *
     * @param limitSpending limit spending.
     * @param sumSpent sum spent
     *
     * @return limit spending object.
     */
    LimitSpending spend(LimitSpending limitSpending, BigDecimal sumSpent);
}
