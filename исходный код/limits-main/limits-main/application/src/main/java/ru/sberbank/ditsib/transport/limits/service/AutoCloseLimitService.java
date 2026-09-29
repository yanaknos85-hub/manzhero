package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.limits.model.limit.Period;

public interface AutoCloseLimitService {
    
    /**
     * Return remains.
     */
    void returnRemains(Period periodNumber);
}
