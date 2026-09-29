package ru.sber.transport.fraud.monitoring.model;

import ru.sber.transport.fraud.monitoring.model.FraudCaseData.FraudMessageItem;
import ru.sber.transport.web.model.FraudMarkerDto;
import ru.sber.transport.web.model.FraudRequestDetailDto;
import ru.sber.transport.web.model.MessagingDto;
import ru.sber.transport.web.model.WaypointDto;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Объект ответа с детальной информацией о заявке с нарушениями
 */

public class WebResponseFraudRequestDetailDto extends FraudRequestDetailDto {

    public WebResponseFraudRequestDetailDto(TripRequestDataWithMessages delegatee) {
        setId(delegatee.getId());
        setHumanReadableId(delegatee.getHumanReadableId());
        setPassenger(new WebResponseEmployeeDto(delegatee.getPassenger()));
        if (delegatee.getApprover() != null) {
            setApprover(new WebResponseEmployeeDto(delegatee.getApprover()));
            setAutoApproval(delegatee.getPassenger().getId().equals(delegatee.getApprover().getId()));
        }

        setDesiredDate(delegatee.getDesiredDate());
        if (delegatee.getDuration() != null) {
            setTripEndDate(delegatee.getDesiredDate().plusSeconds(delegatee.getDuration()));
        }

        setApprovalDate(delegatee.getApprovalDate());
        setTransportType(TransportTypeEnum.valueOf(delegatee.getTransportType()));
        setCostCenter(delegatee.getCostCenter());
        setStatus(delegatee.getRequestStatus());
        setPurpose(new WebPurposeDto(delegatee.getPurpose()));
        setDepartment(delegatee.getDepartment().getName());
        setDepartmentCode(delegatee.getDepartment().getCode());
        setPlannedCost(delegatee.getPlannedCost());
        setActualCost(delegatee.getActualCost());
        setDistance(delegatee.getDistance());
        var fraudMarkers = delegatee.getFrauds()
                .stream()
                .map(WebFraudMarkerWithMessagesDto::new)
                .map(x -> (FraudMarkerDto) x)
                .toList();
        var messages = delegatee.getFraudMessages().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        messagesList -> messagesList.getValue().stream()
                                .map(this::toMessagingDto)
                                .toList()));
        fraudMarkers.forEach(fraudMarker ->
                fraudMarker.setMessaging(messages.getOrDefault(fraudMarker.getId(), Collections.emptyList())));
        setFraudMarkers(fraudMarkers);

        var intermediateAddresses = delegatee.getWaypoints().size() <= 2
                ? List.<WaypointDto>of()
                : delegatee.getWaypoints().stream()
                .skip(1)
                .limit(delegatee.getWaypoints().size() - 2L)
                .map(WebResponseWaypointDto::new)
                .map(x -> (WaypointDto) x)
                .toList();

        setIntermediateAddresses(intermediateAddresses);
        setWaypointsCount(delegatee.getWaypoints().size());
        setDepartureAddress(delegatee.getDepartureAddress());
        setDestinationAddress(delegatee.getDestinationAddress());
        setCompensationType(delegatee.getCompensationType());
    }

    private MessagingDto toMessagingDto(FraudMessageItem messageItem) {
        return new MessagingDto(messageItem.getFromEmail(),
                messageItem.getToEmail(),
                messageItem.getMessageDate().atZone(ZoneId.of("Europe/Moscow"))
                        .toOffsetDateTime(),
                messageItem.getBody());
    }
}
