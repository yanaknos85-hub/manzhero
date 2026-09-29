package ru.sber.transport.fraud.monitoring.business.model;

import ru.sber.transport.fraud.monitoring.model.TransportType;
import ru.sber.transport.fraud.monitoring.model.TripRequest;
import ru.sber.transport.fraud.monitoring.model.Waypoint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


public record TestTripRequest(
        UUID id,
        String humanReadableId,
        TransportType transportType,
        String tariff,
        UUID passengerId,
        UUID approverId,
        OffsetDateTime desiredDate,
        OffsetDateTime approvalDate,
        BigDecimal plannedCost,
        BigDecimal actualCost,
        UUID organizationId,
        UUID departmentId,
        String requestStatus,
        UUID purposeId,
        String timeZone,
        List<Waypoint> waypoints,
        Double distance,
        String compensationType,
        Long duration
) implements TripRequest {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getHumanReadableId() {
        return humanReadableId;
    }

    @Override
    public TransportType getTransportType() {
        return transportType;
    }

    @Override
    public String getTariff() {
        return tariff;
    }

    @Override
    public UUID getPassengerId() {
        return passengerId;
    }

    @Override
    public UUID getApproverId() {
        return approverId;
    }

    @Override
    public OffsetDateTime getDesiredDate() {
        return desiredDate;
    }

    @Override
    public OffsetDateTime getApprovalDate() {
        return approvalDate;
    }

    @Override
    public BigDecimal getPlannedCost() {
        return plannedCost;
    }

    @Override
    public BigDecimal getActualCost() {
        return actualCost;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    @Override
    public UUID getDepartmentId() {
        return departmentId;
    }

    @Override
    public String getRequestStatus() {
        return requestStatus;
    }

    @Override
    public UUID getPurposeId() {
        return purposeId;
    }

    @Override
    public String getTimeZone() {
        return timeZone;
    }

    @Override
    public List<Waypoint> getWaypoints() {
        return waypoints != null ? waypoints : List.of();
    }

    @Override
    public Double getDistance() {
        return distance;
    }

    @Override
    public String getCompensationType() {
        return compensationType;
    }

    @Override
    public Long getDuration() {
        return duration;
    }
}