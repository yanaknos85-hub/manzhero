package ru.sberbank.ditsib.transport.limits.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
class ApproveLimitRequestMapperTest {
    
    private final ApproveLimitRequestMapper mapper = new ApproveLimitRequestMapperImpl(new SumMapperImpl());
    private final Approver approver = new Approver();
    private final UUID employeeRequestId = UUID.randomUUID();
    private final UUID employeeApproverId = UUID.randomUUID();
    private final UUID employeeDepartmentId = UUID.randomUUID();
    private final UUID departmentId = UUID.randomUUID();
    
    @Test
    @DisplayName("Модель в сообщение")
    void toMessage() {
        
        var limitRequest = LimitRequest.builder()
                                                   .limitType(LimitType.DEPARTMENT)
                                                   .humanReadableId("YYH")
                                                   .id(UUID.randomUUID())
                                                   .approverList(List.of(approver))
                                                   .author(Employee.builder().id(employeeRequestId).build())
                                                   .creationTime(LocalDateTime.now().minusDays(3))
                                                   .declineReason("ppppppp")
                                                   .description("ooooooooo")
                                                   .periodData(PeriodData.DECEMBER)
                                                   .status(LimitRequestStatus.INIT)
                                                   .sum(BigDecimal.valueOf(1000))
                                                   .transportType(TransportTypeEnum.TAXI)
                                                   .year(2021)
                                                   .build();
    
        var approver = Approver.builder()
                           .limitRequest(limitRequest)
                           .approvalDate(LocalDateTime.now().minusDays(9))
                           .approvalState(ApprovalState.APPROVED)
                           .department(Department.builder().id(departmentId)
                                                 .departmentHead(
                                                         Employee.builder()
                                                                 .id(employeeDepartmentId)
                                                                 .build()
                                                 ).build())
                           .employee(Employee.builder().id(employeeApproverId).build())
                           .id(UUID.randomUUID())
                           .sum(BigDecimal.valueOf(500))
                           .build();
    
        var actual = mapper.toMessage(approver);
        assertThat(actual.id()).isEqualTo(approver.getId());
        assertThat(actual.approvalDate()).isEqualTo(approver.getApprovalDate());
        assertThat(actual.approvalState()).isEqualTo(approver.getApprovalState().name());
        assertThat(actual.creationTime()).isEqualTo(limitRequest.getCreationTime());
        assertThat(actual.status()).isEqualTo(limitRequest.getStatus().name());
        assertThat(actual.limitType()).isEqualTo(limitRequest.getLimitType().getName());
        assertThat(actual.authorId()).isEqualTo(limitRequest.getAuthor().getId());
        assertThat(actual.departmentHeadId()).isEqualTo(approver.getDepartment().getDepartmentHead().getId());
        assertThat(actual.declineReason()).isEqualTo(limitRequest.getDeclineReason());
        assertThat(actual.sum()).isEqualTo(approver.getSum().multiply(BigDecimal.valueOf(100)).longValue());
        assertThat(actual.description()).isEqualTo(limitRequest.getDescription());
        assertThat(actual.employeeId()).isEqualTo(approver.getEmployee().getId());
        assertThat(actual.humanReadableId()).isEqualTo(limitRequest.getHumanReadableId());
        assertThat(actual.limitRequestId()).isEqualTo(approver.getLimitRequest().getId());
        assertThat(actual.departmentId()).isEqualTo(approver.getDepartment().getId());
        assertThat(actual.organizationDepartmentId()).isEqualTo(approver.getDepartment().getOrganizationId());
        assertThat(actual.organizationEmployeeId()).isEqualTo(approver.getEmployee().getOrganizationId());
        assertThat(actual.period()).isEqualTo(limitRequest.getPeriod().name());
        assertThat(actual.sumLimit()).isEqualTo(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue());
        assertThat(actual.year()).isEqualTo(limitRequest.getYear());
        assertThat(actual.deleted()).isFalse();
    }
    
}