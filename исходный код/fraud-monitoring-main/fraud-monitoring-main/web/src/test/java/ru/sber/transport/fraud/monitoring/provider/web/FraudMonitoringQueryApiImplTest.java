package ru.sber.transport.fraud.monitoring.provider.web;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.HttpStatus;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.fraud.monitoring.web.query.FraudMonitoringQueryApiImpl;
import ru.sber.transport.web.api.FraudMonitoringQueryApi;
import ru.sber.transport.web.model.GetAllFraudRequestsRequest;
import ru.sber.transport.web.model.RegistryFilterRequest;
import ru.sber.transport.web.model.Sort;
import ru.sber.transport.web.model.SortDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static ru.sber.transport.fraud.monitoring.model.FraudCaseData.FraudMessageItem;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка контроллера для получения заявок с нарушениями")
class FraudMonitoringQueryApiImplTest {
    private final TripRequestsService tripRequestsService = mock(TripRequestsService.class);

    private final FraudMonitoringQueryApi controller = new FraudMonitoringQueryApiImpl(tripRequestsService);

    @Test
    @DisplayName("Успешное получение заявки по ID")
    void getFraudRequestById_shouldReturnRequest() throws Exception {
        final var requestId = UUID.randomUUID();
        final var tripRequestData = createTestTripRequestDataWithMessages(requestId);

        when(tripRequestsService.get(requestId)).thenReturn(Optional.of(tripRequestData));

        final var resultFuture = controller.getFraudRequestById(requestId);
        final var result = resultFuture.get();

        assertSoftly(it -> {
            it.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            it.assertThat(result.getBody()).isNotNull();
            assertNotNull(result.getBody());
            it.assertThat(result.getBody().getId()).isEqualTo(requestId);
        });

        verify(tripRequestsService).get(requestId);
    }

    @Test
    @DisplayName("Получение заявки по несуществующему ID")
    void getFraudRequestById_shouldThrowExceptionWhenNotFound() {
        final var requestId = UUID.randomUUID();
        when(tripRequestsService.get(requestId)).thenReturn(Optional.empty());

        final var resultFuture = controller.getFraudRequestById(requestId);

        assertThatThrownBy(resultFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TripRequest")
                .hasMessageContaining(requestId.toString());

        verify(tripRequestsService).get(requestId);
    }

    @Test
    @DisplayName("Успешное получение всех заявок с пагинацией")
    void getAllFraudRequests_shouldReturnPage() throws Exception {
        final var passengerId = UUID.randomUUID();
        final var approverId = UUID.randomUUID();

        var registryFilterRequest = new RegistryFilterRequest();
        registryFilterRequest.setApprover(List.of(approverId));
        registryFilterRequest.setPassenger(List.of(passengerId));

        final var getAllRequest = new GetAllFraudRequestsRequest()
                .page(0)
                .size(2)
                .sort("humanReadableId")
                .direction(SortDirection.ASC)
                .filter(registryFilterRequest);

        final var tripRequestDataList = List.of(
                createTestTripRequestData(UUID.randomUUID()),
                createTestTripRequestData(UUID.randomUUID())
        );

        final var page = new TestTripRequestPage(
                tripRequestDataList,
                0,
                2,
                5,
                true,
                "humanReadableId"
        );

        when(tripRequestsService.get(
                any(WebRequestFilter.class),
                eq(0),
                eq(2),
                eq("humanReadableId"),
                eq(true)
        )).thenReturn(page);

        final var resultFuture = controller.getAllFraudRequests(getAllRequest);
        final var result = resultFuture.get();

        assertSoftly(it -> {
            it.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            it.assertThat(result.getBody()).isNotNull();
            assertNotNull(result.getBody());
            it.assertThat(result.getBody().getContent()).hasSize(2);
            it.assertThat(result.getBody().getPage().getNumber()).isEqualTo(0);
            it.assertThat(result.getBody().getPage().getSize()).isEqualTo(2);
            it.assertThat(result.getBody().getPage().getTotal()).isEqualTo(5);
            it.assertThat(result.getBody().getPage().isFirst()).isTrue();
            it.assertThat(result.getBody().getPage().isLast()).isFalse();
            it.assertThat(result.getBody().getSort().getField()).isEqualTo("humanReadableId");
            it.assertThat(result.getBody().getSort().getDirection()).isEqualTo(Sort.DirectionEnum.ASC);
        });

        verify(tripRequestsService).get(
                any(WebRequestFilter.class),
                eq(0),
                eq(2),
                eq("humanReadableId"),
                eq(true)
        );
    }

    private TripRequestData createTestTripRequestData(UUID id) {
        return new TestTripRequestData(
                id,
                "PUBLIC",
                "OT-0001-000001",
                "NEW",
                createTestEmployee(),
                createTestEmployee(),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1),
                new BigDecimal("100.00"),
                new BigDecimal("80.00"),
                "CC-001",
                createTestTripPurpose(),
                createTestDepartment(),
                createTestWaypoints(),
                Collections.emptyList(),
                150.0,
                "Departure address 1",
                "Destination address 1",
                "COMPENSATION_TYPE",
                120L
        );
    }

    private TripRequestDataWithMessages createTestTripRequestDataWithMessages(UUID id) {
        var fraudCaseId = UUID.randomUUID();
        var messaging1 = new TestFraudMessageItem(
                LocalDateTime.of(2026, 6, 6, 12, 35, 11),
                "tech@sbrf.ru",
                "pavlov@sbrf.ru",
                "тело письма"
        );
        var messaging2 = new TestFraudMessageItem(
                LocalDateTime.of(2026, 6, 6, 12, 46, 11),
                "pavlov@sbrf.ru",
                "tech@sbrf.ru",
                "тело письма"
        );
        var fraudCaseData = new TestFraudCaseData(
                fraudCaseId,
                "Недостаточно информации для принятия решения",
                "Комментарий ИИ",
                "Комментарий",
                true,
                List.of(messaging1, messaging2)
        );
        return new TestTripRequestDataWithMessages(
                id,
                "PUBLIC",
                "OT-0001-000001",
                "NEW",
                createTestEmployee(),
                createTestEmployee(),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1),
                new BigDecimal("100.00"),
                new BigDecimal("80.00"),
                "CC-001",
                createTestTripPurpose(),
                createTestDepartment(),
                createTestWaypoints(),
                List.of(fraudCaseData),
                Map.of(fraudCaseId, List.of(messaging1, messaging2)),
                150.0,
                "Departure address 1",
                "Destination address 1",
                "COMPENSATION_TYPE",
                120L
        );
    }

    private Employee createTestEmployee() {
        return new Employee() {
            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }

            @Override
            public UUID getDepartmentId() {
                return UUID.randomUUID();
            }

            @Override
            public UUID getOrganizationId() {
                return UUID.randomUUID();
            }

            @Override
            public String getFirstName() {
                return "FirstName";
            }

            @Override
            public String getLastName() {
                return "LastName";
            }

            @Override
            public String getPatronymic() {
                return "Patronymic";
            }

            @Override
            public UUID getPositionId() {
                return UUID.randomUUID();
            }

            @Override
            public String getCostCenter() {
                return "00LL000000";
            }

            @Override
            public String getPersonnelNumber() {
                return "000001";
            }
        };
    }

    private TripPurpose createTestTripPurpose() {
        return new TripPurpose() {
            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }

            @Override
            public String getLabel() {
                return "Trip purpose";
            }
        };
    }

    private Department createTestDepartment() {
        return new Department() {
            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }

            @Override
            public UUID getHeadId() {
                return null;
            }

            @Override
            public UUID getParentId() {
                return null;
            }

            @Override
            public String getName() {
                return "Department name";
            }

            @Override
            public String getCode() {
                return "Department code";
            }
        };
    }

    private List<Waypoint> createTestWaypoints() {
        return List.of(
                new Waypoint() {
                    @Override
                    public UUID getId() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public UUID getTripRequestId() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public String getCountry() {
                        return "Country";
                    }

                    @Override
                    public String getRegion() {
                        return "Region";
                    }

                    @Override
                    public String getCity() {
                        return "City";
                    }

                    @Override
                    public String getStreet() {
                        return "Street";
                    }

                    @Override
                    public String getHouse() {
                        return "1";
                    }

                    @Override
                    public String getStructure() {
                        return "A";
                    }

                    @Override
                    public String getBuilding() {
                        return "1";
                    }

                    @Override
                    public Integer getOrderingIndex() {
                        return 0;
                    }

                    @Override
                    public Long getWaitTime() {
                        return 300000L;
                    }

                    @Override
                    public String getAddress() {
                        return "Address";
                    }
                },
                new Waypoint() {
                    @Override
                    public UUID getId() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public UUID getTripRequestId() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public String getCountry() {
                        return "Country";
                    }

                    @Override
                    public String getRegion() {
                        return "Region";
                    }

                    @Override
                    public String getCity() {
                        return "City";
                    }

                    @Override
                    public String getStreet() {
                        return "Street";
                    }

                    @Override
                    public String getHouse() {
                        return "2";
                    }

                    @Override
                    public String getStructure() {
                        return "B";
                    }

                    @Override
                    public String getBuilding() {
                        return "2";
                    }

                    @Override
                    public Integer getOrderingIndex() {
                        return 1;
                    }

                    @Override
                    public Long getWaitTime() {
                        return 600000L;
                    }

                    @Override
                    public String getAddress() {
                        return "Address";
                    }
                }
        );
    }

    private record TestTripRequestData(
            UUID id,
            String transportType,
            String humanReadableId,
            String requestStatus,
            Employee passenger,
            Employee approver,
            OffsetDateTime desiredDate,
            OffsetDateTime approvalDate,
            BigDecimal plannedCost,
            BigDecimal actualCost,
            String costCenter,
            TripPurpose purpose,
            Department department,
            List<Waypoint> waypoints,
            List<Fraud> frauds,
            Double distance,
            String departureAddress,
            String destinationAddress,
            String compensationType,
            Long duration
    ) implements TripRequestData {

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getTransportType() {
            return transportType;
        }

        @Override
        public String getHumanReadableId() {
            return humanReadableId;
        }

        @Override
        public String getRequestStatus() {
            return requestStatus;
        }

        @Override
        public Employee getPassenger() {
            return passenger;
        }

        @Override
        public Employee getApprover() {
            return approver;
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
        public String getCostCenter() {
            return costCenter;
        }

        @Override
        public TripPurpose getPurpose() {
            return purpose;
        }

        @Override
        public Department getDepartment() {
            return department;
        }

        @Override
        public List<Waypoint> getWaypoints() {
            return waypoints != null ? waypoints : List.of();
        }

        @Override
        public List<Fraud> getFrauds() {
            return frauds != null ? frauds : List.of();
        }

        @Override
        public Double getDistance() {
            return distance;
        }

        @Override
        public String getDepartureAddress() {
            return departureAddress;
        }

        @Override
        public String getDestinationAddress() {
            return destinationAddress;
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

    private record TestTripRequestDataWithMessages(
            UUID id,
            String transportType,
            String humanReadableId,
            String requestStatus,
            Employee passenger,
            Employee approver,
            OffsetDateTime desiredDate,
            OffsetDateTime approvalDate,
            BigDecimal plannedCost,
            BigDecimal actualCost,
            String costCenter,
            TripPurpose purpose,
            Department department,
            List<Waypoint> waypoints,
            List<FraudCaseData> frauds,
            Map<UUID, List<FraudMessageItem>> fraudMessages,
            Double distance,
            String departureAddress,
            String destinationAddress,
            String compensationType,
            Long duration
    ) implements TripRequestDataWithMessages {

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getTransportType() {
            return transportType;
        }

        @Override
        public String getHumanReadableId() {
            return humanReadableId;
        }

        @Override
        public String getRequestStatus() {
            return requestStatus;
        }

        @Override
        public Employee getPassenger() {
            return passenger;
        }

        @Override
        public Employee getApprover() {
            return approver;
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
        public String getCostCenter() {
            return costCenter;
        }

        @Override
        public TripPurpose getPurpose() {
            return purpose;
        }

        @Override
        public Department getDepartment() {
            return department;
        }

        @Override
        public List<Waypoint> getWaypoints() {
            return waypoints != null ? waypoints : List.of();
        }

        @Override
        public List<FraudCaseData> getFrauds() {
            return frauds != null ? frauds : List.of();
        }

        @Override
        public Map<UUID, List<FraudMessageItem>> getFraudMessages() {
            return fraudMessages != null ? fraudMessages : Map.of();
        }

        @Override
        public Double getDistance() {
            return distance;
        }

        @Override
        public String getDepartureAddress() {
            return departureAddress;
        }

        @Override
        public String getDestinationAddress() {
            return destinationAddress;
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

    private record TestFraudCaseData(
            UUID id,
            String aiVerdict,
            String aiComment,
            String comment,
            boolean needValidation,
            List<FraudMessageItem> messaging
    ) implements FraudCaseData {

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getAiVerdict() {
            return aiVerdict;
        }

        @Override
        public String getAiComment() {
            return aiComment;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public boolean isNeedValidation() {
            return needValidation;
        }

        @Override
        public List<? extends FraudMessageItem> getMessaging() {
            return messaging;
        }
    }

    private record TestFraudMessageItem(
            LocalDateTime messageDate,
            String fromEmail,
            String toEmail,
            String body
    ) implements FraudMessageItem {

        @Override
        public LocalDateTime getMessageDate() {
            return messageDate;
        }

        @Override
        public String getFromEmail() {
            return fromEmail;
        }

        @Override
        public String getToEmail() {
            return toEmail;
        }

        @Override
        public String getBody() {
            return body;
        }
    }

    private record TestTripRequestPage(
            List<TripRequestData> content,
            int pageNumber,
            int pageSize,
            int totalElements,
            boolean ascending,
            String sortField
    ) implements Page<TripRequestData> {

        @Override
        public List<TripRequestData> content() {
            return content;
        }

        @Override
        public PageData page() {
            return new PageData() {
                @Override
                public int number() {
                    return pageNumber;
                }

                @Override
                public int size() {
                    return pageSize;
                }

                @Override
                public boolean last() {
                    return pageNumber >= (totalElements - 1) / pageSize;
                }

                @Override
                public boolean first() {
                    return pageNumber == 0;
                }

                @Override
                public int total() {
                    return totalElements;
                }

                @Override
                public int count() {
                    return content.size();
                }
            };
        }

        @Override
        public SortData sort() {
            return new SortData() {
                @Override
                public String field() {
                    return sortField;
                }

                @Override
                public boolean asc() {
                    return ascending;
                }
            };
        }
    }
}
