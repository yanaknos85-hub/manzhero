package ru.sberbank.ditsib.transport.approvals.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.SharedRideApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.fraud.FraudCommentDTO;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Маппер согласования на поездку.
 */
@Slf4j
@Mapper(uses = {EmployeeMapper.class})
public abstract class TripApprovalMapper {

    @Mapping(target = "passenger", source = "approval.passenger")
    @Mapping(target = "waypoints", source = "approval.waypoints")
    @Mapping(target = "expectedTime", source = "approval.expectedTime")
    @Mapping(target = "expectedDistance", source = "approval.expectedDistance")
    @Mapping(target = "passengerCount", source = "approval.passengerCount")
    @Mapping(target = "requestHumanReadableId", source = "approval.requestHumanReadableId")
    @Mapping(target = "requestId", source = "approval.actionId")
    @Mapping(target = "reason", source = "approval.reason")
    @Mapping(target = "type", source = "approval.type")
    @Mapping(target = "creationTime", source = "approval.creationTime")
    public abstract TripApproveDTO toApproveDTO(BaseRequestApproval approval);


    List<FraudCommentDTO> toFraudComment(List<FraudData> fraudData) {
        if (CollectionUtils.isEmpty(fraudData)) {
            return Collections.emptyList();
        }
        return fraudData.stream()
                .filter(f -> f.getType() != null)
                .collect(Collectors.toMap(
                        FraudData::getType,
                        f -> f,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .map(data -> new FraudCommentDTO(data.getComment(), null, null))
                .toList();
    }

    @InheritConfiguration
    public abstract TripApproveDTO fillApproveDTO(
            BaseRequestApproval approval,
            @MappingTarget TripApproveDTO target
    );

    public TripApproveDTO toApproveDTO(TripRequestApproval approval) {
        var dto = toApproveDTO((BaseRequestApproval) approval);
        dto.setCoopTrip(approval.getSharedRideId() != null);
        dto.setFraudComment(toFraudComment(approval.getFraudData()));
        return dto;
    }

    public TripApproveDTO toApproveDTO(FinalTripApproval approval) {
        return toApproveDTO((BaseRequestApproval) approval);
    }

    public SharedRideApproveDTO toApproveDTO(SharedRideJoinApproval approval) {
        final SharedRideApproveDTO tripApproveDTO =
                (SharedRideApproveDTO) fillApproveDTO(approval, new SharedRideApproveDTO());
        tripApproveDTO.setAddRequestId(approval.getAddRequestId());
        tripApproveDTO.setRequestId(approval.getActionId());
        return tripApproveDTO;
    }

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    public abstract List<Map<String, Object>> toWaypointDto(List<Map<String, Object>> waypoints);

    Map<String, Object> toWaypointDto(Map<String, Object> waypoints) {
        var address = waypoints.get("address");
        var result = new HashMap<String, Object>();
        if (address instanceof Map<?, ?>) {
            ((Map<String, Object>) address).forEach((key, value) -> result.put(String.valueOf(key), value));
        } else {
            log.info("No address found. Waypoints: " + waypoints);
        }
        return result;
    }

    @Mapping(target = "actionId", source = "id")
    @Mapping(target = "actorId", source = "passengerId")
    @Mapping(target = "endTime", source = "finishedTime")
    @Mapping(target = "cost", source = "expected.cost")
    @Mapping(target = "expectedTime", source = "expected.time")
    @Mapping(target = "expectedDistance", source = "expected.distance")
    @Mapping(target = "requestHumanReadableId", source = "humanReadableId")
    @Mapping(target = "taxiClass", source = "tripClass")
    @Mapping(target = "waypoints", expression = "java(toWaypoint(message.getWaypoints()))")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deadline", ignore = true)
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "fraudData", ignore = true)
    public abstract BaseRequestApproval toBaseRequestApproval(
            RequestMessage message,
            @MappingTarget BaseRequestApproval approval
    );

    @InheritConfiguration
    @Mapping(target = "sharedRideId", source = "rideId")
    @Mapping(target = "fraudData", ignore = true)
    public abstract void toTripRequestApproval(
            @MappingTarget TripRequestApproval approval,
            RequestMessage message
    );

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    public abstract List<Map<String, Object>> toWaypoint(List<RequestMessage.Waypoint> waypoints);

    Map<String, Object> toWaypoint(RequestMessage.Waypoint waypoint) {
        return objectMapper().convertValue(waypoint, new TypeReference<>() {
        });
    }

    @Lookup
    ObjectMapper objectMapper() {
        return null;
    }
}
