package ru.sberbank.ditsib.transport.limits.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.EmpLimit;

import java.util.List;
import java.util.UUID;

/**
 * Repository for working with limits.
 */
@Repository
@Transactional(readOnly = true)
public interface EmpLimitRepository extends LimitRepository<EmpLimit> {
    
    /**
     * Find limit by employee.
     *
     * @param employee employee
     *
     * @return limit.
     */
    List<EmpLimit> findByEmployee(Employee employee);
    
    /**
     * Find limit by employee.
     *
     * @param employeeId employee
     *
     * @return limit.
     */
    List<EmpLimit> findByEmployeeId(UUID employeeId);
    
    /**
     * Find limit by employee.
     *
     * @param employeeId employeeId
     * @param year year
     * @param limitStatus limitStatus
     *
     * @return limit.
     */
    List<EmpLimit> findByEmployeeIdAndYearAndLimitStatusNot(UUID employeeId, Integer year, LimitStatus limitStatus);
    
    /**
     * Find limit by employee.
     *
     * @param employeeId employeeId
     * @param year year
     * @param limitStatus limitStatus
     *
     * @return limit.
     */
    List<EmpLimit> findByEmployeeIdAndYearAndLimitServiceTypeAndLimitStatusNot(UUID employeeId, Integer year,
                                                                               String limitServiceType,
                                                                               LimitStatus limitStatus);
    
    /**
     * Find limit by employee.
     * @param employeeId employeeId
     * @param year year
     * @param limitStatus limitStatus
     * @param limitType limitType
     * @return limit
     */
    List<EmpLimit> findByEmployeeIdAndYearAndLimitStatusAndLimitType(UUID employeeId
            , Integer year, LimitStatus limitStatus, LimitType limitType);
}

