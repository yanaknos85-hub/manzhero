package ru.sberbank.ditsib.transport.approvals.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.approvals.constant.ApprovalJournalType;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.projection.ApprovalJournalProjection;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.approvals.dto.fraud.FraudCommentDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class ApprovalJournalMapperTest {

    private final ApprovalJournalMapper approvalJournalMapper = Mappers.getMapper(ApprovalJournalMapper.class);

    @Test
    void sharedRideJoinApprovalToApprovalJournal() {
        var source = Instancio.create(SharedRideJoinApproval.class);
        var id = UUID.randomUUID();
        var expected = new ApprovalJournal();
        expected.setId(id);
        expected.setApprovalId(source.getId());
        expected.setActorId(source.getActorId());
        expected.setActionId(source.getActionId());
        expected.setStatus(source.getStatus());
        expected.setCreationTime(source.getCreationTime());
        expected.setApprovedById(source.getApprovedById());
        expected.setTransportType(source.getTransportType());
        expected.setTaxiClass(source.getTaxiClass());
        expected.setDesiredDate(source.getDesiredDate());
        expected.setTripPurposeId(source.getPurposeId());
        expected.setExpectedCost(source.getCost());
        expected.setExpectedTime(source.getExpectedTime());
        expected.setExpectedDistance(source.getExpectedDistance());
        expected.setHumanReadableId(source.getRequestHumanReadableId());
        expected.setPassengerCount(source.getPassengerCount());
        expected.setWaypoints(source.getWaypoints());
        expected.setSharedRideId(null);
        expected.setAddRequestId(source.getAddRequestId());
        expected.setTimeZone(source.getTimeZone());
        expected.setType(ApprovalJournalType.SHARED_RIDE_JOIN);

        var actual = approvalJournalMapper.sharedRideJoinApprovalToApprovalJournal(source, id);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(approvalJournalMapper.sharedRideJoinApprovalToApprovalJournal(null, null)).isNull();
    }

    @Test
    void tripRequestApprovalToApprovalJournal() {
        var source = Instancio.create(TripRequestApproval.class);
        var id = UUID.randomUUID();
        var expected = new ApprovalJournal();
        expected.setId(id);
        expected.setApprovalId(source.getId());
        expected.setActorId(source.getActorId());
        expected.setActionId(source.getActionId());
        expected.setStatus(source.getStatus());
        expected.setCreationTime(source.getCreationTime());
        expected.setApprovedById(source.getApprovedById());
        expected.setTransportType(source.getTransportType());
        expected.setTaxiClass(source.getTaxiClass());
        expected.setDesiredDate(source.getDesiredDate());
        expected.setTripPurposeId(source.getPurposeId());
        expected.setExpectedCost(source.getCost());
        expected.setExpectedTime(source.getExpectedTime());
        expected.setExpectedDistance(source.getExpectedDistance());
        expected.setHumanReadableId(source.getRequestHumanReadableId());
        expected.setPassengerCount(source.getPassengerCount());
        expected.setWaypoints(source.getWaypoints());
        expected.setSharedRideId(source.getSharedRideId());
        expected.setAddRequestId(null);
        expected.setTimeZone(source.getTimeZone());
        expected.setType(ApprovalJournalType.TRIP_REQUEST);

        var actual = approvalJournalMapper.tripRequestApprovalToApprovalJournal(source, id);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(approvalJournalMapper.tripRequestApprovalToApprovalJournal(null, null)).isNull();
    }

    @Test
    void finalTripApprovalToApprovalJournal() {
        var source = Instancio.create(FinalTripApproval.class);
        var id = UUID.randomUUID();
        var expected = new ApprovalJournal();
        expected.setId(id);
        expected.setApprovalId(source.getId());
        expected.setActorId(source.getActorId());
        expected.setActionId(source.getActionId());
        expected.setStatus(source.getStatus());
        expected.setCreationTime(source.getCreationTime());
        expected.setApprovedById(source.getApprovedById());
        expected.setTransportType(source.getTransportType());
        expected.setTaxiClass(source.getTaxiClass());
        expected.setDesiredDate(source.getDesiredDate());
        expected.setTripPurposeId(source.getPurposeId());
        expected.setExpectedCost(source.getCost());
        expected.setExpectedTime(source.getExpectedTime());
        expected.setExpectedDistance(source.getExpectedDistance());
        expected.setHumanReadableId(source.getRequestHumanReadableId());
        expected.setPassengerCount(source.getPassengerCount());
        expected.setWaypoints(source.getWaypoints());
        expected.setSharedRideId(null);
        expected.setAddRequestId(null);
        expected.setTimeZone(source.getTimeZone());
        expected.setType(ApprovalJournalType.FINAL_TRIP);

        var actual = approvalJournalMapper.finalTripApprovalToApprovalJournal(source, id);


        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(approvalJournalMapper.finalTripApprovalToApprovalJournal(null, null)).isNull();
    }

    @Test
    void updateTripRequestApprovalToApprovalJournal() {
        var source = Instancio.create(UpdateTripRequestApproval.class);
        var id = UUID.randomUUID();
        var expected = new ApprovalJournal();
        expected.setId(id);
        expected.setApprovalId(source.getId());
        expected.setActorId(source.getActorId());
        expected.setActionId(source.getActionId());
        expected.setStatus(source.getStatus());
        expected.setCreationTime(source.getCreationTime());
        expected.setApprovedById(source.getApprovedById());
        expected.setTransportType(source.getTransportType());
        expected.setTaxiClass(source.getTaxiClass());
        expected.setDesiredDate(source.getDesiredDate());
        expected.setTripPurposeId(source.getPurposeId());
        expected.setExpectedCost(source.getCost());
        expected.setExpectedTime(source.getExpectedTime());
        expected.setExpectedDistance(source.getExpectedDistance());
        expected.setHumanReadableId(source.getRequestHumanReadableId());
        expected.setPassengerCount(source.getPassengerCount());
        expected.setWaypoints(source.getWaypoints());
        expected.setSharedRideId(null);
        expected.setAddRequestId(null);
        expected.setTimeZone(source.getTimeZone());
        expected.setType(ApprovalJournalType.UPDATE_TRIP_REQUEST);

        var actual = approvalJournalMapper.updateTripRequestApprovalToApprovalJournal(source, id);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(approvalJournalMapper.updateTripRequestApprovalToApprovalJournal(null, null)).isNull();
    }

    @Test
    void approvalJournalProjectionToApprovalJournalDto() {
        var fraudDataList = Instancio.ofList(FraudData.class)
                .size(2)
                .create();
        var expectedFraudComment = fraudDataList.stream()
                .map(fraudData -> new FraudCommentDTO(fraudData.getComment(), null, null))
                .toList();
        Map<String, Object> address1 = Map.of(
                "id", "b9fa1186-0820-4a2a-bc89-1eddb08faef3",
                "city", "Москва",
                "house", "32 к1",
                "region", "Москва",
                "street", "Кутузовский проспект",
                "country", "Россия",
                "latitude", 55.74134604533717,
                "longitude", 37.53081783098175,
                "existInVspGosbTbRegistry", false
        );
        Map<String, Object> address2 = Map.of(
                "id", "2032b2c6-f951-46aa-bfb5-e98b0848d08a",
                "city", "Москва",
                "house", "19а",
                "region", "Москва",
                "street", "Сумской проезд",
                "country", "Россия",
                "latitude", 55.63188965748339,
                "longitude", 37.59622928721561,
                "existInVspGosbTbRegistry", false
        );

        var waypoints = List.of(Map.of(
                "id", "437f95fe-6f60-4f4b-801a-5334fe436e6f",
                "address", address1),
                Map.of(
                        "id", "0fcc7cdf-89e7-4ef4-a17e-0699e45a0c41",
                        "address", address2));
        var approvalId = UUID.randomUUID();
        var projection = createApprovalJournalProjection(approvalId, waypoints);
        var expectedPassenger = new EmployeeDTO(
                projection.getEmployeeId(),
                projection.getEmployeeFirstName(),
                projection.getEmployeeLastName(),
                projection.getEmployeePatronymic(),
                projection.getEmployeePersonnelNumber(),
                projection.getEmployeeDepartmentId(),
                projection.getEmployeeUserId()
        );
        var expected = Instancio.of(ApprovalJournalDto.class)
                .set(field(ApprovalJournalDto::addRequestId), projection.getAddRequestId())
                .set(field(ApprovalJournalDto::cost), projection.getCost())
                .set(field(ApprovalJournalDto::desiredDate), projection.getDesiredDate())
                .set(field(ApprovalJournalDto::expectedDistance), projection.getExpectedDistance())
                .set(field(ApprovalJournalDto::expectedTime), projection.getExpectedTime())
                .set(field(ApprovalJournalDto::fraudComment), expectedFraudComment)
                .set(field(ApprovalJournalDto::id), projection.getId())
                .set(field(ApprovalJournalDto::isCoopTrip), projection.getIsCoopTrip())
                .set(field(ApprovalJournalDto::passengerCount), projection.getPassengerCount())
                .set(field(ApprovalJournalDto::passenger), expectedPassenger)
                .set(field(ApprovalJournalDto::purposeId), projection.getPurposeId())
                .set(field(ApprovalJournalDto::purposeLabel), projection.getPurposeLabel())
                .set(field(ApprovalJournalDto::requestHumanReadableId), projection.getRequestHumanReadableId())
                .set(field(ApprovalJournalDto::requestId), projection.getRequestId())
                .set(field(ApprovalJournalDto::status), projection.getStatus())
                .set(field(ApprovalJournalDto::taxiClass), projection.getTaxiClass())
                .set(field(ApprovalJournalDto::timeZone), projection.getTimeZone())
                .set(field(ApprovalJournalDto::transportType), projection.getTransportType())
                .set(field(ApprovalJournalDto::type), projection.getType())
                .set(field(ApprovalJournalDto::waypoints), List.of(address1, address2))
                .create();
        var actual = approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(projection, fraudDataList);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("fraudComment")
                .isEqualTo(expected);
        assertThat(approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(null, null)).isNull();
    }

    private ApprovalJournalProjection createApprovalJournalProjection(UUID id, List<Map<String, Object>> waypoints) {
        return new ApprovalJournalProjection() {
            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public UUID getEmployeeId() {
                return UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
            }

            @Override
            public String getEmployeeFirstName() {
                return "Петр";
            }

            @Override
            public String getEmployeeLastName() {
                return "Петров";
            }

            @Override
            public String getEmployeePatronymic() {
                return null;
            }

            @Override
            public String getEmployeePersonnelNumber() {
                return "2016497";
            }

            @Override
            public UUID getEmployeeDepartmentId() {
                return UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8");
            }

            @Override
            public UUID getEmployeeUserId() {
                return UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
            }

            @Override
            public UUID getRequestId() {
                return UUID.fromString("575aabe3-7b74-4c7a-abc1-7383203579dc");
            }

            @Override
            public String getTransportType() {
                return "PERSONAL";
            }

            @Override
            public String getTaxiClass() {
                return null;
            }

            @Override
            public LocalDateTime getDesiredDate() {
                return LocalDateTime.of(2024, 11, 1, 12, 0, 0);
            }

            @Override
            public UUID getPurposeId() {
                return UUID.fromString("9ae969e4-3369-459e-8505-7784dd7d7250");
            }

            @Override
            public String getPurposeLabel() {
                return "Первая V 1.0";
            }

            @Override
            public double getCost() {
                return 10400.0;
            }

            @Override
            public String getStatus() {
                return "APPROVED";
            }

            @Override
            public List<Map<String, Object>> getWaypoints() {
                return waypoints;
            }

            @Override
            public Duration getExpectedTime() {
                return Duration.ofMillis(862000000000L);
            }

            @Override
            public double getExpectedDistance() {
                return 6.609;
            }

            @Override
            public int getPassengerCount() {
                return 1;
            }

            @Override
            public String getRequestHumanReadableId() {
                return "OT-0001-00018542";
            }

            @Override
            public boolean getIsCoopTrip() {
                return false;
            }

            @Override
            public String getType() {
                return "SHARED_RIDE_JOIN";
            }

            @Override
            public String getTimeZone() {
                return "GMT+03:00";
            }

            @Override
            public UUID getAddRequestId() {
                return UUID.fromString("575aabe3-7b74-4c7a-abc1-7383203579dc");
            }
        };
    }
}
