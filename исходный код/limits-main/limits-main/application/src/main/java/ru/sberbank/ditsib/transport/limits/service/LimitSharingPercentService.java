package ru.sberbank.ditsib.transport.limits.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with limit sharing procents.
 */
public interface LimitSharingPercentService {
    
    /**
     * Add limit sharing per period.
     *
     * @param limitSharingPercents limit sharing per period.
     */
    LimitSharingPercents add(LimitSharingPercents limitSharingPercents);
    
    /**
     * Save limit sharing per period.
     *
     * @param limitSharingPercents limit sharing per period.
     */
    void save(LimitSharingPercents limitSharingPercents);
    
    /**
     * Delete limit sharing per period.
     *
     * @param limitSharingPercents limit sharing per period.
     */
    void delete(LimitSharingPercents limitSharingPercents);
    
    /**
     * Get limit sharing per period.
     *
     * @param id ID of limit sharing per period.
     *
     * @return limit sharing per period.
     */
    Optional<LimitSharingPercents> get(UUID id);
    
    /**
     * Get all limit sharings per period.
     *
     * @return list of limit sharing per period.
     */
    List<LimitSharingPercents> getAll();

    Page<LimitSharingPercents> getAll(Integer page, Integer size, Sort.Direction direction, UUID limitId);
    
    /**
     * Get limit sharing procents by limit.
     *
     * @param limit limit
     *
     * @return limit sharing procents
     */
    Optional<LimitSharingPercents> getByLimit(Limit limit);
}
