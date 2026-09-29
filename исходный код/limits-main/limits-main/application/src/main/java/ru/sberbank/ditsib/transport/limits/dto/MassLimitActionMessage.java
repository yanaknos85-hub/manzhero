package ru.sberbank.ditsib.transport.limits.dto;

import java.util.List;

public record MassLimitActionMessage(
        List<LimitActionMessage> actionMessages,
        String action,
        String reservationStrategy
) {}
