package ru.sber.transport.fraud.monitoring.model;

import ru.sber.transport.web.model.FraudMarkerDto;
import ru.sber.transport.web.model.FraudRequestDto;
import ru.sber.transport.web.model.WaypointDto;

/**
 * Объект ответа с данными о заявке с нарушениями
 */
public class WebFraudRequestDto extends FraudRequestDto {

    public WebFraudRequestDto(TripRequestData delegatee) {
        setId(delegatee.getId());
        setHumanReadableId(delegatee.getHumanReadableId());
        setDesiredDate(delegatee.getDesiredDate());
        setApprovalDate(delegatee.getApprovalDate());
        setTransportType(TransportTypeEnum.valueOf(delegatee.getTransportType()));
        setPassenger(new WebResponseEmployeeDto(delegatee.getPassenger()));
        if (delegatee.getApprover() != null) {
            setApprover(new WebResponseEmployeeDto(delegatee.getApprover()));
            setAutoApproval(delegatee.getPassenger().getId().equals(delegatee.getApprover().getId()));
        }
        if (delegatee.getPurpose() != null) {
            setPurpose(new WebPurposeDto(delegatee.getPurpose()));
        }

        setStatus(delegatee.getRequestStatus());
        setPlannedCost(delegatee.getPlannedCost());
        setActualCost(delegatee.getActualCost());
        setDepartment(delegatee.getDepartment().getName());
        var fraudMarkers = delegatee.getFrauds()
                .stream()
                .map(WebFraudMarkerDto::new)
                .map(x -> (FraudMarkerDto) x)
                .toList();
        setFraudMarkers(fraudMarkers);

        var waypoints = delegatee.getWaypoints().stream()
                .map(WebResponseWaypointDto::new)
                .map(x -> (WaypointDto) x)
                .toList();

        setIntermediatePointsCount(waypoints.size() - 2);
        setDepartureAddress(delegatee.getDepartureAddress());
        setDestinationAddress(delegatee.getDestinationAddress());

    }
}
