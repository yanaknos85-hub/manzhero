package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.instancio.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.sberbank.ditsib.transport.approvals.constant.JournalSortProperty;
import ru.sberbank.ditsib.transport.approvals.database.dao.ApprovalJournalRepository;
import ru.sberbank.ditsib.transport.approvals.database.dao.FraudDataRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.projection.ApprovalJournalProjection;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.Type;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;
import ru.sberbank.ditsib.transport.approvals.exception.InvalidSortPropertyException;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalJournalMapper;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeRightService;
import ru.sberbank.ditsib.transport.approvals.services.EmployeeService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @InjectMocks
    private SearchServiceImpl searchService;
    @Mock
    private EmployeeRightService employeeRightService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private ApprovalJournalMapper approvalJournalMapper;
    @Mock
    private ApprovalJournalRepository approvalJournalRepository;
    @Mock
    private FraudDataRepository fraudDataRepository;

    @Test
    void searchForJournal_shouldReturnEmptyPage_WhenNoApprovals() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);
        var transportType = "TAXI";
        var pageable = PageRequest.of(0, 10);
        var employeeSearchParams = Instancio.of(EmployeeSearchParams.class).create();
        var allowedDepartments = Map.of("DEPT_1", Set.of(UUID.randomUUID()));
        var emptyPage = Page.empty();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(emptyPage).when(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                anyBoolean(),
                anySet(),
                any(UUID.class),
                any(Pageable.class));

        var result = searchService.searchForJournal(userId, statuses, types, transportType, pageable, employeeSearchParams);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPageable().getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    void searchForJournal_secondPage_shouldReturnEmptyPage_WhenNoApprovals() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);
        var transportType = "TAXI";
        var pageable = PageRequest.of(1, 10);
        var employeeSearchParams = Instancio.of(EmployeeSearchParams.class).create();
        var departmentIds = Set.of(UUID.randomUUID());
        var employeeIds = List.of(UUID.randomUUID());
        var allowedDepartments = Map.of("DEPT_1", departmentIds);
        var emptyPage = Page.empty();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(employeeIds).when(employeeService).getAllByDepartmentIds(departmentIds);
        doReturn(emptyPage).when(approvalJournalRepository).findAllForJournal(
                List.of("NEW"),
                employeeIds,
                false,
                Collections.singleton("TAXI"),
                userId,
                PageRequest.of(1, 10, Sort.by(JournalSortProperty.CREATION_TIME.getSortName()).descending()));

        var result = searchService.searchForJournal(userId, statuses, types, transportType, pageable, employeeSearchParams);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPageable().getSort()).isEqualTo(Sort.unsorted());
    }

    @SneakyThrows
    @Test
    void searchForJournal_shouldReturnPageWithApprovals() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW, Status.EDITED);
        var types = List.of(Type.FINAL_TRIP, Type.TRIP_REQUEST);
        var transportType = "TAXI";
        var pageable = PageRequest.of(0, 10);
        var employeeSearchParams = Instancio.of(EmployeeSearchParams.class).create();
        var departmentId = UUID.randomUUID();
        var allowedDepartments = Map.of("DEPT_1", Set.of(departmentId));
        var approvalId = UUID.randomUUID();
        var projection = createApprovalJournalProjection(approvalId, Instancio.create(new TypeToken<List<Map<String, Object>>>() {}));
        var page = new PageImpl<>(List.of(projection), pageable, 1);
        var fraudId = UUID.randomUUID();
        var fraudData = createFraudData(fraudId, approvalId);
        var fraudList = List.of(fraudData);
        var expected = createApprovalJournalDto(approvalId, Instancio.create(new TypeToken<>() {
        }));
        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(page).when(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                anyBoolean(),
                anySet(),
                any(UUID.class),
                any(Pageable.class));
        doReturn(fraudList).when(fraudDataRepository).findAllByApprovalIdIn(any());
        doReturn(expected).when(approvalJournalMapper).approvalJournalProjectionToApprovalJournalDto(projection,
                fraudList);

        var result = searchService.searchForJournal(userId, statuses, types, transportType, pageable, employeeSearchParams);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void searchForJournal_shouldReturnPageWithApprovalsEmptyWaypoints() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW, Status.EDITED);
        var types = List.of(Type.FINAL_TRIP, Type.TRIP_REQUEST);
        var transportType = "TAXI";
        var pageable = PageRequest.of(0, 10);
        var employeeSearchParams = Instancio.of(EmployeeSearchParams.class).create();
        var departmentId = UUID.randomUUID();
        var allowedDepartments = Map.of("DEPT_1", Set.of(departmentId));
        var approvalId = UUID.randomUUID();
        var projection = createApprovalJournalProjection(approvalId, Collections.emptyList());
        var page = new PageImpl<>(List.of(projection), pageable, 1);
        var fraudId = UUID.randomUUID();
        var fraudData = createFraudData(fraudId, approvalId);
        var fraudList = List.of(fraudData);
        var expected = createApprovalJournalDto(approvalId, Instancio.create(new TypeToken<>() {
        }));

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(page).when(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                anyBoolean(),
                anySet(),
                any(UUID.class),
                any(Pageable.class));
        doReturn(fraudList).when(fraudDataRepository).findAllByApprovalIdIn(any());
        doReturn(expected).when(approvalJournalMapper).approvalJournalProjectionToApprovalJournalDto(projection,
                fraudList);

        var result = searchService.searchForJournal(userId, statuses, types, transportType, pageable, employeeSearchParams);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void searchForJournal_shouldPassCorrectParametersToRepository() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);
        var transportType = "TAXI";
        var pageable = PageRequest.of(0, 10);
        var employeeSearchParams = Instancio.of(EmployeeSearchParams.class).create();
        var departmentId = UUID.randomUUID();
        var allowedDepartments = Map.of("DEPT_1", Set.of(departmentId));
        var emptyPage = Page.empty();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(emptyPage).when(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                anyBoolean(),
                anySet(),
                any(UUID.class),
                any(Pageable.class));

        searchService.searchForJournal(userId, statuses, types, transportType, pageable, employeeSearchParams);

        verify(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                eq(false),
                anySet(),
                eq(userId),
                any(Pageable.class)
        );
    }

    @Test
    void searchForJournal_shouldHandleNullStatuses() {
        var userId = UUID.randomUUID();
        var types = List.of(Type.FINAL_TRIP);
        var transportType = "TAXI";
        var pageable = PageRequest.of(0, 10);
        var employeeSearchParams = Instancio.of(EmployeeSearchParams.class).create();
        var departmentId = UUID.randomUUID();
        var allowedDepartments = Map.of("DEPT_1", Set.of(departmentId));
        var emptyPage = Page.empty();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(emptyPage).when(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                anyBoolean(),
                anySet(),
                any(UUID.class),
                any(Pageable.class));

        searchService.searchForJournal(userId, null, types, transportType, pageable, employeeSearchParams);

        verify(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                eq(false),
                anySet(),
                eq(userId),
                any(Pageable.class)
        );
    }

    @Test
    void searchForJournal_shouldHandleNullTransportType() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);

        var pageable = PageRequest.of(0, 10);
        var employeeSearchParams = Instancio.create(EmployeeSearchParams.class);
        var departmentIds = Set.of(UUID.randomUUID());
        var employeeIds = List.of(UUID.randomUUID());
        var allowedDepartments = Map.of("", departmentIds);
        var emptyPage = Page.empty();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(employeeIds).when(employeeService).getAllByDepartmentIds(departmentIds);
        doReturn(emptyPage).when(approvalJournalRepository).findAllForJournal(
                List.of("NEW"),
                employeeIds,
                true,
                null,
                userId,
                PageRequest.of(0, 10, Sort.by(JournalSortProperty.CREATION_TIME.getSortName()).descending()));

        searchService.searchForJournal(userId, statuses, types, null, pageable, employeeSearchParams);

        verify(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                eq(true),
                eq(null),
                eq(userId),
                any(Pageable.class)
        );
    }

    @ParameterizedTest
    @MethodSource("searchForJournal_checkSort")
    void searchForJournal_checkSort(String sortValue, String jpaName) {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);

        var pageable = PageRequest.of(0, 10, Sort.by(sortValue).descending());
        var employeeSearchParams = Instancio.create(EmployeeSearchParams.class);
        var allowedDepartments = Collections.emptyMap();
        var emptyPage = Page.empty();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(emptyPage).when(approvalJournalRepository).findAllForJournal(
                List.of("NEW"),
                Collections.emptyList(),
                true,
                null,
                userId,
                PageRequest.of(0, 10, Sort.by(jpaName).descending()));

        searchService.searchForJournal(userId, statuses, types, null, pageable, employeeSearchParams);

        verify(approvalJournalRepository).findAllForJournal(
                anyList(),
                anyList(),
                eq(true),
                eq(null),
                eq(userId),
                any(Pageable.class)
        );
    }

    private static Stream<Arguments> searchForJournal_checkSort() {
        return Stream.of(
                Arguments.of(
                        JournalSortProperty.ID.getSortName(),
                        JournalSortProperty.ID.getSortName(),
                        JournalSortProperty.ID.getColumnName() + " " + Sort.Direction.DESC.name()),
                Arguments.of(
                        JournalSortProperty.DESIRED_DATE.getSortName(),
                        JournalSortProperty.DESIRED_DATE.getSortName(),
                        JournalSortProperty.DESIRED_DATE.getColumnName() + " " + Sort.Direction.DESC.name()),
                Arguments.of(
                        JournalSortProperty.STATUS.getSortName(),
                        JournalSortProperty.STATUS.getSortName(),
                        JournalSortProperty.STATUS.getColumnName() + " " + Sort.Direction.DESC.name()),
                Arguments.of(
                        JournalSortProperty.TRANSPORT_TYPE.getSortName(),
                        JournalSortProperty.TRANSPORT_TYPE.getSortName(),
                        JournalSortProperty.TRANSPORT_TYPE.getColumnName() + " " + Sort.Direction.DESC.name()),
                Arguments.of(
                        JournalSortProperty.COST.getSortName(),
                        JournalSortProperty.COST.getSortName(),
                        JournalSortProperty.COST.getColumnName() + " " + Sort.Direction.DESC.name()),
                Arguments.of(
                        JournalSortProperty.CREATION_TIME.getSortName(),
                        JournalSortProperty.CREATION_TIME.getSortName(),
                        JournalSortProperty.CREATION_TIME.getColumnName() + " " + Sort.Direction.DESC.name())
        );
    }

    @Test
    void searchForJournal_invalidSortProperty() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);

        var pageable = PageRequest.of(0, 10, Sort.by("personnelNumber").descending());
        var employeeSearchParams = Instancio.create(EmployeeSearchParams.class);
        var allowedDepartments = Collections.emptyMap();

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);

        assertThatThrownBy(() -> searchService.searchForJournal(userId, statuses, types, null, pageable, employeeSearchParams))
                .isInstanceOf(InvalidSortPropertyException.class)
                .hasMessage("Указаны недопустимые свойства сортировок");

        verifyNoInteractions(approvalJournalRepository);
    }

    @Test
    void searchCount_shouldReturnCount() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW, Status.EDITED);
        var types = List.of(Type.FINAL_TRIP);
        var departmentId = UUID.randomUUID();
        var allowedDepartments = Map.of("DEPT_1", Set.of(departmentId));
        var expectedCount = 5L;

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(expectedCount).when(approvalJournalRepository).countForJournal(any(), any(), anyBoolean(), any(), any());

        var result = searchService.searchCount(statuses, userId, types);

        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    void searchCount_shouldPassCorrectParametersToRepository() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);
        var departmentId = UUID.randomUUID();
        var allowedDepartments = Map.of("DEPT_1", Set.of(departmentId));

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(0L).when(approvalJournalRepository).countForJournal(any(), any(), anyBoolean(), any(), any());

        searchService.searchCount(statuses, userId, types);

        verify(approvalJournalRepository).countForJournal(
                anyList(),
                anyList(),
                eq(false),
                anySet(),
                eq(userId)
        );
    }

    @Test
    void searchCount_shouldHandleNullTransportType() {
        var userId = UUID.randomUUID();
        var statuses = List.of(Status.NEW);
        var types = List.of(Type.FINAL_TRIP);
        var allowedTypes = Set.of("TAXI", "BUS");
        var allowedDepartments = Map.of("DEPT_1", allowedTypes);

        doReturn(allowedDepartments).when(employeeRightService).getAllowedDepartments(userId);
        doReturn(0L).when(approvalJournalRepository).countForJournal(any(), any(), anyBoolean(), any(), any());

        searchService.searchCount(statuses, userId, types);

        verify(approvalJournalRepository).countForJournal(
                anyList(),
                anyList(),
                eq(false),
                anySet(),
                eq(userId)
        );
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

    private FraudData createFraudData(UUID id, UUID approvalId) {
        return Instancio.of(FraudData.class)
                .set(field(FraudData::getId), id)
                .set(field(FraudData::getApproval), Instancio.of(TripRequestApproval.class)
                        .set(field(TripRequestApproval::getId), approvalId)
                        .create())
                .create();
    }

    private ApprovalJournalDto createApprovalJournalDto(UUID id, List<Map<String, Object>> waypoints) {
        return Instancio.of(ApprovalJournalDto.class)
                .set(field(ApprovalJournalDto::id), id)
                .set(field(ApprovalJournalDto::waypoints), waypoints)
                .create();
    }

    @Test
    void saveApprovalJournalForSharedRideJoinApproval() {
        var savedApproval1 = Instancio.create(SharedRideJoinApproval.class);
        var savedApproval2 = Instancio.create(SharedRideJoinApproval.class);
        var approvalJournal = Instancio.create(ApprovalJournal.class);
        doReturn(Optional.of(approvalJournal)).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval1.getId());
        doReturn(Optional.empty()).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval2.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).sharedRideJoinApprovalToApprovalJournal(savedApproval1, approvalJournal.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).sharedRideJoinApprovalToApprovalJournal(savedApproval2, null);
        doReturn(approvalJournal).when(approvalJournalRepository).save(approvalJournal);

        searchService.saveApprovalJournalForSharedRideJoinApproval(savedApproval1);
        searchService.saveApprovalJournalForSharedRideJoinApproval(savedApproval2);

        verify(approvalJournalRepository, times(2)).findApprovalJournalByApprovalId(any(UUID.class));
        verify(approvalJournalMapper).sharedRideJoinApprovalToApprovalJournal(any(SharedRideJoinApproval.class), any(UUID.class));
        verify(approvalJournalMapper).sharedRideJoinApprovalToApprovalJournal(any(SharedRideJoinApproval.class), isNull());
        verify(approvalJournalRepository, times(2)).save(any(ApprovalJournal.class));
    }

    @Test
    void saveApprovalJournalForTripRequestApproval() {
        var savedApproval1 = Instancio.create(TripRequestApproval.class);
        var savedApproval2 = Instancio.create(TripRequestApproval.class);
        var approvalJournal = Instancio.create(ApprovalJournal.class);
        doReturn(Optional.of(approvalJournal)).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval1.getId());
        doReturn(Optional.empty()).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval2.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).tripRequestApprovalToApprovalJournal(savedApproval1, approvalJournal.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).tripRequestApprovalToApprovalJournal(savedApproval2, null);
        doReturn(approvalJournal).when(approvalJournalRepository).save(approvalJournal);

        searchService.saveApprovalJournalForTripRequestApproval(savedApproval1);
        searchService.saveApprovalJournalForTripRequestApproval(savedApproval2);

        verify(approvalJournalRepository, times(2)).findApprovalJournalByApprovalId(any(UUID.class));
        verify(approvalJournalMapper).tripRequestApprovalToApprovalJournal(any(TripRequestApproval.class), any(UUID.class));
        verify(approvalJournalMapper).tripRequestApprovalToApprovalJournal(any(TripRequestApproval.class), isNull());
        verify(approvalJournalRepository, times(2)).save(any(ApprovalJournal.class));
    }

    @Test
    void saveApprovalJournalForFinalTripApproval() {
        var savedApproval1 = Instancio.create(FinalTripApproval.class);
        var savedApproval2 = Instancio.create(FinalTripApproval.class);
        var approvalJournal = Instancio.create(ApprovalJournal.class);
        doReturn(Optional.of(approvalJournal)).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval1.getId());
        doReturn(Optional.empty()).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval2.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).finalTripApprovalToApprovalJournal(savedApproval1, approvalJournal.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).finalTripApprovalToApprovalJournal(savedApproval2, null);
        doReturn(approvalJournal).when(approvalJournalRepository).save(approvalJournal);

        searchService.saveApprovalJournalForFinalTripApproval(savedApproval1);
        searchService.saveApprovalJournalForFinalTripApproval(savedApproval2);

        verify(approvalJournalRepository, times(2)).findApprovalJournalByApprovalId(any(UUID.class));
        verify(approvalJournalMapper).finalTripApprovalToApprovalJournal(any(FinalTripApproval.class), any(UUID.class));
        verify(approvalJournalMapper).finalTripApprovalToApprovalJournal(any(FinalTripApproval.class), isNull());
        verify(approvalJournalRepository, times(2)).save(any(ApprovalJournal.class));
    }

    @Test
    void saveApprovalJournalForUpdateTripRequestApproval() {
        var savedApproval1 = Instancio.create(UpdateTripRequestApproval.class);
        var savedApproval2 = Instancio.create(UpdateTripRequestApproval.class);
        var approvalJournal = Instancio.create(ApprovalJournal.class);
        doReturn(Optional.of(approvalJournal)).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval1.getId());
        doReturn(Optional.empty()).when(approvalJournalRepository).findApprovalJournalByApprovalId(savedApproval2.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).updateTripRequestApprovalToApprovalJournal(savedApproval1, approvalJournal.getId());
        doReturn(approvalJournal).when(approvalJournalMapper).updateTripRequestApprovalToApprovalJournal(savedApproval2, null);
        doReturn(approvalJournal).when(approvalJournalRepository).save(approvalJournal);

        searchService.saveApprovalJournalForUpdateTripRequestApproval(savedApproval1);
        searchService.saveApprovalJournalForUpdateTripRequestApproval(savedApproval2);

        verify(approvalJournalRepository, times(2)).findApprovalJournalByApprovalId(any(UUID.class));
        verify(approvalJournalMapper).updateTripRequestApprovalToApprovalJournal(any(UpdateTripRequestApproval.class), any(UUID.class));
        verify(approvalJournalMapper).updateTripRequestApprovalToApprovalJournal(any(UpdateTripRequestApproval.class), isNull());
        verify(approvalJournalRepository, times(2)).save(any(ApprovalJournal.class));
    }
}