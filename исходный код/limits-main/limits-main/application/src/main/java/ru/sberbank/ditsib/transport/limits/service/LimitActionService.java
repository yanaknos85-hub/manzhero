package ru.sberbank.ditsib.transport.limits.service;

import ru.sberbank.ditsib.transport.dto.limits.ReservationStrategy;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionResultMessage;

import java.util.List;
import java.util.UUID;

/**
 * Service for working with limits reservation.
 */
public interface LimitActionService {
    
    LimitActionResultMessage processLimitAction(LimitActionMessage message);
    
    
    MassLimitActionResultMessage processMassLimitAction(UUID userId,
                                                        List<LimitActionMessage> messages,
                                                        ReservationStrategy reservationStrategy,
                                                        String action);

    MassLimitActionResultMessage processMassLimitAction(UUID userId, MassLimitActionMessage massLimitActionMessage);
}
