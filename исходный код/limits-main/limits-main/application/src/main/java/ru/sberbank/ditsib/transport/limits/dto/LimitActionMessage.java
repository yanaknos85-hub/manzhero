package ru.sberbank.ditsib.transport.limits.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.*;
import java.util.*;

@Getter
@Builder
@Accessors(fluent = true)
public final class LimitActionMessage {
    private final UUID organizationId;
    private final String action;
    private final UUID departmentId;
    private final UUID employeeId;
    private final String transportType;
    private final Long sum;
    private final Long bonusSum;
    private final Integer moneySaved;
    private final LocalDateTime plannedDate;
    private final UUID requestId;
    private final Boolean checkLimit;
    private final String humanReadableId;
    private final boolean coop;
    private final boolean driver;
}
