package ru.sber.transport.limits.business.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.limits.business.model.Status;

import java.util.List;
import java.util.UUID;

/**
 * Ошибка закрытия лимита
 */
@RequiredArgsConstructor
@Getter
public class ClosingStatusNotAllowedException extends RuntimeException {

    private final UUID limitId;

    private final Status status;
    
    private final List<Status> allowedStatuses;

}
