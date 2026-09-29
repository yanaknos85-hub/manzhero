package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.transport.approvals.database.model.DepLimit;
import ru.sberbank.ditsib.transport.approvals.mappers.DepLimitMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.DepLimitListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitMessage;
import ru.sberbank.ditsib.transport.approvals.services.DepLimitService;

/**
 * Реализация слушателя лимита департамента.
 */
@RequiredArgsConstructor
public class DepLimitListenerImpl implements DepLimitListener {
    
    public static final String DEPARTMENT_TYPE = "DEPARTMENT";
    private final DepLimitService depLimitService;
    
    private final DepLimitMapper mapper;
    
    @Override
    public void handle(LimitMessage message) {
        if (!DEPARTMENT_TYPE.equals(message.getLimitType())) {
            return;
        }
        if (message.isDeleted()) {
            var depLimit = depLimitService.get(message.getId());
            if(depLimit.isPresent()){
                depLimit.get().setDeleted(true);
                depLimitService.save(depLimit.get());
            }
        } else {
            DepLimit limit = depLimitService.get(message.getId()).orElse(new DepLimit());
            mapper.toDepLimit(message, limit);
            depLimitService.save(limit);
        }
    }
}
