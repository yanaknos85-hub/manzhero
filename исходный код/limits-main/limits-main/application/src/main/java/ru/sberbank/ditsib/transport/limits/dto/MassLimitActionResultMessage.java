package ru.sberbank.ditsib.transport.limits.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MassLimitActionResultMessage(
        List<LimitActionResultMessage> resultMessages,
        String limitReservationStatus,
        String message
) {
}
