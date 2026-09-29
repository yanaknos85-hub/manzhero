package ru.sberbank.ditsib.transport.approvals.mappers;

import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.projection.ApprovalJournalProjection;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.fraud.FraudCommentDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper
public interface ApprovalJournalMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "approvalId", source = "source.id")
    @Mapping(target = "actorId", source = "source.actorId")
    @Mapping(target = "actionId", source = "source.actionId")
    @Mapping(target = "status", source = "source.status")
    @Mapping(target = "creationTime", source = "source.creationTime")
    @Mapping(target = "approvedById", source = "source.approvedById")
    @Mapping(target = "transportType", source = "source.transportType")
    @Mapping(target = "taxiClass", source = "source.taxiClass")
    @Mapping(target = "desiredDate", source = "source.desiredDate")
    @Mapping(target = "tripPurposeId", source = "source.purposeId")
    @Mapping(target = "expectedCost", source = "source.cost")
    @Mapping(target = "expectedTime", source = "source.expectedTime")
    @Mapping(target = "expectedDistance", source = "source.expectedDistance")
    @Mapping(target = "humanReadableId", source = "source.requestHumanReadableId")
    @Mapping(target = "passengerCount", source = "source.passengerCount")
    @Mapping(target = "waypoints", source = "source.waypoints")
    @Mapping(target = "sharedRideId", ignore = true)
    @Mapping(target = "addRequestId", source = "source.addRequestId")
    @Mapping(target = "timeZone", source = "source.timeZone")
    @Mapping(target = "type", constant = "SHARED_RIDE_JOIN")
    ApprovalJournal sharedRideJoinApprovalToApprovalJournal(SharedRideJoinApproval source, UUID id);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "approvalId", source = "source.id")
    @Mapping(target = "actorId", source = "source.actorId")
    @Mapping(target = "actionId", source = "source.actionId")
    @Mapping(target = "status", source = "source.status")
    @Mapping(target = "creationTime", source = "source.creationTime")
    @Mapping(target = "approvedById", source = "source.approvedById")
    @Mapping(target = "transportType", source = "source.transportType")
    @Mapping(target = "taxiClass", source = "source.taxiClass")
    @Mapping(target = "desiredDate", source = "source.desiredDate")
    @Mapping(target = "tripPurposeId", source = "source.purposeId")
    @Mapping(target = "expectedCost", source = "source.cost")
    @Mapping(target = "expectedTime", source = "source.expectedTime")
    @Mapping(target = "expectedDistance", source = "source.expectedDistance")
    @Mapping(target = "humanReadableId", source = "source.requestHumanReadableId")
    @Mapping(target = "passengerCount", source = "source.passengerCount")
    @Mapping(target = "waypoints", source = "source.waypoints")
    @Mapping(target = "sharedRideId", source = "source.sharedRideId")
    @Mapping(target = "addRequestId", ignore = true)
    @Mapping(target = "timeZone", source = "source.timeZone")
    @Mapping(target = "type", constant = "TRIP_REQUEST")
    ApprovalJournal tripRequestApprovalToApprovalJournal(TripRequestApproval source, UUID id);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "approvalId", source = "source.id")
    @Mapping(target = "actorId", source = "source.actorId")
    @Mapping(target = "actionId", source = "source.actionId")
    @Mapping(target = "status", source = "source.status")
    @Mapping(target = "creationTime", source = "source.creationTime")
    @Mapping(target = "approvedById", source = "source.approvedById")
    @Mapping(target = "transportType", source = "source.transportType")
    @Mapping(target = "taxiClass", source = "source.taxiClass")
    @Mapping(target = "desiredDate", source = "source.desiredDate")
    @Mapping(target = "tripPurposeId", source = "source.purposeId")
    @Mapping(target = "expectedCost", source = "source.cost")
    @Mapping(target = "expectedTime", source = "source.expectedTime")
    @Mapping(target = "expectedDistance", source = "source.expectedDistance")
    @Mapping(target = "humanReadableId", source = "source.requestHumanReadableId")
    @Mapping(target = "passengerCount", source = "source.passengerCount")
    @Mapping(target = "waypoints", source = "source.waypoints")
    @Mapping(target = "sharedRideId", ignore = true)
    @Mapping(target = "addRequestId", ignore = true)
    @Mapping(target = "timeZone", source = "source.timeZone")
    @Mapping(target = "type", constant = "FINAL_TRIP")
    ApprovalJournal finalTripApprovalToApprovalJournal(FinalTripApproval source, UUID id);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "approvalId", source = "source.id")
    @Mapping(target = "actorId", source = "source.actorId")
    @Mapping(target = "actionId", source = "source.actionId")
    @Mapping(target = "status", source = "source.status")
    @Mapping(target = "creationTime", source = "source.creationTime")
    @Mapping(target = "approvedById", source = "source.approvedById")
    @Mapping(target = "transportType", source = "source.transportType")
    @Mapping(target = "taxiClass", source = "source.taxiClass")
    @Mapping(target = "desiredDate", source = "source.desiredDate")
    @Mapping(target = "tripPurposeId", source = "source.purposeId")
    @Mapping(target = "expectedCost", source = "source.cost")
    @Mapping(target = "expectedTime", source = "source.expectedTime")
    @Mapping(target = "expectedDistance", source = "source.expectedDistance")
    @Mapping(target = "humanReadableId", source = "source.requestHumanReadableId")
    @Mapping(target = "passengerCount", source = "source.passengerCount")
    @Mapping(target = "waypoints", source = "source.waypoints")
    @Mapping(target = "sharedRideId", ignore = true)
    @Mapping(target = "addRequestId", ignore = true)
    @Mapping(target = "timeZone", source = "source.timeZone")
    @Mapping(target = "type", constant = "UPDATE_TRIP_REQUEST")
    ApprovalJournal updateTripRequestApprovalToApprovalJournal(UpdateTripRequestApproval source, UUID id);

    @Mapping(target = "passenger.id", source = "source.employeeId")
    @Mapping(target = "passenger.firstName", source = "source.employeeFirstName")
    @Mapping(target = "passenger.lastName", source = "source.employeeLastName")
    @Mapping(target = "passenger.patronymic", source = "source.employeePatronymic")
    @Mapping(target = "passenger.personnelNumber", source = "source.employeePersonnelNumber")
    @Mapping(target = "passenger.departmentId", source = "source.employeeDepartmentId")
    @Mapping(target = "passenger.userId", source = "source.employeeUserId")
    @Mapping(target = "waypoints", source = "source.waypoints", qualifiedByName = "toWaypointsDto")
    @Mapping(target = "expectedTime", source = "source.expectedTime")
    @Mapping(target = "fraudComment", source = "fraudComment", qualifiedByName = "toFraudComment")
    ApprovalJournalDto approvalJournalProjectionToApprovalJournalDto(ApprovalJournalProjection source,
                                                                     List<FraudData> fraudComment);

    @Named("toFraudComment")
    default List<FraudCommentDTO> toFraudComment(List<FraudData> fraudDataList) {
        if (CollectionUtils.isEmpty(fraudDataList)) {
            return Collections.emptyList();
        }
        return fraudDataList.stream()
                .filter(fraudData -> fraudData.getType() != null)
                .collect(Collectors.toMap(FraudData::getType, fraudData -> fraudData, (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .map(fraudData -> new FraudCommentDTO(fraudData.getComment(), null, null))
                .toList();
    }

    @Named("toWaypointsDto")
    default List<Map<String, Object>> toWaypointsDto(List<Map<String, Object>> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            return Collections.emptyList();
        } else {
            return waypoints.stream()
                    .map(this::extractAddressMap)
                    .toList();
        }
    }

    private Map<String, Object> extractAddressMap(Map<String, Object> waypoint) {
        var address = waypoint.get("address");
        if (!(address instanceof Map<?, ?> addressMap)) {
            return Collections.emptyMap();
        } else {
            return addressMap.entrySet().stream()
                    .collect(Collectors.toMap(entry -> String.valueOf(entry.getKey()), Map.Entry::getValue));
        }
    }
}