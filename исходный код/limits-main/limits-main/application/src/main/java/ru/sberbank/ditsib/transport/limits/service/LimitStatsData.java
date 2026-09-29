package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Data of limit statistics.
 */
public interface LimitStatsData {

    /**
     * Load data for statistics.
     *
     * @param limitLevelDTOs limit level datas.
     * @param untilDate load data until date.
     */
    void load(List<LimitLevelDTO> limitLevelDTOs, LocalDate untilDate);

    /**
     * Get a loaded department.
     *
     * @param limitId ID of a limit.
     * @return the found department.
     */
    Department department(UUID limitId);

    /**
     * Get spendings from loaded data. Available to use spendings of a limit just one time.
     *
     * @param limitId ID of the limit to get spendings.
     * @return list of spendings.
     */
    LinkedList<LimitSpending> popSpends(UUID limitId);

    /**
     * Get count of active employees` of a department.
     *
     * @param departmentId ID of the department to get employees` count.
     * @return employees` count.
     */
    long employees(UUID departmentId);

    /**
     * Get date until which data was loaded.
     *
     * @return date until which data was loaded.
     */
    LocalDate untilDate();

    /**
     * Get start of year date until which data was loaded.
     *
     * @return get start of year date until which data was loaded.
     */
    LocalDate startOfYear();
}
