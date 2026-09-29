package ru.sber.transport.fraud.monitoring.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Фильтр заявок на поездку
 */
@Builder
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public class WebRequestFilter implements RequestFilter {

    private List<String> transportType;

    private final String approverName;

    private final String passengerName;

    private final List<UUID> passenger;

    private final List<UUID> approver;

    private OffsetDateTime tripDateStart;

    private OffsetDateTime tripDateEnd;

    private OffsetDateTime approveDateStart;

    private OffsetDateTime approveDateEnd;

    private final String humanReadableId;

    private List<UUID> purpose;


}
