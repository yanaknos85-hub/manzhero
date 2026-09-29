package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.fraud.monitoring.model.TransportType;
import ru.sber.transport.fraud.monitoring.model.TripRequest;
import ru.sber.transport.fraud.monitoring.model.Waypoint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TripRequestImpl implements TripRequest {
    
    private UUID id;
    private String humanReadableId;
    private TransportType transportType;
    private String tariff;
    private UUID passengerId;
    private UUID approverId;
    private OffsetDateTime desiredDate;
    private OffsetDateTime approvalDate;
    private BigDecimal plannedCost;
    private BigDecimal actualCost;
    private UUID organizationId;
    private UUID departmentId;
    private String requestStatus;
    private UUID purposeId;
    private String timeZone;
    private List<Waypoint> waypoints = new ArrayList<>();
    private Double distance;
    private String compensationType;
    private Long duration;
}
