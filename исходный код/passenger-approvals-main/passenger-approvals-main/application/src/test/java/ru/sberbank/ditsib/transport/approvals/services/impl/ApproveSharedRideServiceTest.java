package ru.sberbank.ditsib.transport.approvals.services.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.ApprovalsApplication;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.services.ApproveService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@DisplayName("Проверка сервиса согласования заявок на присоединение к совместным поездкам")
@SpringBootTest(classes = ApprovalsApplication.class)
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
class ApproveSharedRideServiceTest extends KafkaTest {
    @Autowired
    private ApproveService<SharedRideJoinApproval> approveService;
    @Autowired
    private SharedRideApprovalRepository approvalRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DelegateRepository delegateRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    private Employee employee1;
    private Employee employee2;
    
    @BeforeEach
    public void beforeEach() {
        employee1 = createEmployee();
        employee2 = createEmployee();
    }
    
    @AfterEach
    public void afterEach() {
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
        delegateRepository.deleteAll();
        approvalRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Проверка получения пустого списка согласований")
    void test_empty() {
        var approvals = getActiveApprovalsForUser(employee1.getId());
        assertThat(approvals).isEmpty();
        
        createApproval(employee1.getId(), Status.NEW);
        
        approvals = getActiveApprovalsForUser(employee2.getId());
        assertThat(approvals).isEmpty();
    }
    
    @Test
    @DisplayName("Проверка получения списка согласований")
    void testGetActive() {
        // ************* Create approvals ***************
        SharedRideJoinApproval approval_1_1 = createApproval(employee1.getId(), Status.NEW);
        SharedRideJoinApproval approval_1_2 = createApproval(employee1.getId(), Status.EDITED);
        createApproval(employee1.getId(), Status.APPROVED);
        createApproval(employee1.getId(), Status.CANCELLED);
        createApproval(employee1.getId(), Status.DECLINED);
    
        SharedRideJoinApproval approval_2_1 = createApproval(employee2.getId(), Status.NEW);
        SharedRideJoinApproval approval_2_2 = createApproval(employee2.getId(), Status.NEW);
        SharedRideJoinApproval approval_2_3 = createApproval(employee2.getId(), Status.EDITED);
        createApproval(employee2.getId(), Status.APPROVED);
        createApproval(employee2.getId(), Status.CANCELLED);
        createApproval(employee2.getId(), Status.DECLINED);
        
        assertThat(approvalRepository.count()).isEqualTo(11);
        
        // ************* Check get approvals ***************
        var approvals = getActiveApprovalsForUser(employee1.getId());
        assertThat(approvals).hasSize(2);
        assertThat(toIds(approvals)).containsExactlyInAnyOrder(approval_1_1.getId(), approval_1_2.getId());
    
        approvals = getActiveApprovalsForUser(employee2.getId());
        assertThat(approvals).hasSize(3);
        assertThat(toIds(approvals)).containsExactlyInAnyOrder(approval_2_1.getId(), approval_2_2.getId(),
                                                               approval_2_3.getId());
    }
    
    @Test
    @DisplayName("Проверка получения списка закрытых согласований")
    void testGetClosed() {
        // ************* Create approvals ***************
        createApproval(employee1.getId(), Status.NEW);
        createApproval(employee1.getId(), Status.EDITED);
        SharedRideJoinApproval approval_1_3 = createApproval(employee1.getId(), Status.APPROVED);
        SharedRideJoinApproval approval_1_4 = createApproval(employee1.getId(), Status.CANCELLED);
        SharedRideJoinApproval approval_1_5 = createApproval(employee1.getId(), Status.DECLINED);
        
        createApproval(employee2.getId(), Status.NEW);
        createApproval(employee2.getId(), Status.NEW);
        createApproval(employee2.getId(), Status.EDITED);
        SharedRideJoinApproval approval_2_4 = createApproval(employee2.getId(), Status.APPROVED);
        SharedRideJoinApproval approval_2_5 = createApproval(employee2.getId(), Status.CANCELLED);
        SharedRideJoinApproval approval_2_6 = createApproval(employee2.getId(), Status.DECLINED);
        SharedRideJoinApproval approval_2_7 = createApproval(employee2.getId(), Status.DECLINED);
        
        assertThat(approvalRepository.count()).isEqualTo(12);
        
        // ************* Check get approvals ***************
        var approvals = getClosedApprovalsForUser(employee1.getId());
        assertThat(approvals).hasSize(3);
        assertThat(toIds(approvals)).containsExactlyInAnyOrder(approval_1_3.getId(), approval_1_4.getId(),
                                                               approval_1_5.getId());
        
        approvals = getClosedApprovalsForUser(employee2.getId());
        assertThat(approvals).hasSize(4);
        assertThat(toIds(approvals)).containsExactlyInAnyOrder(approval_2_4.getId(), approval_2_5.getId(),
                                                               approval_2_6.getId(), approval_2_7.getId());
    }
    
    private List<UUID> toIds(Collection<? extends TripApproveDTO> approvals) {
        return approvals.stream().map(TripApproveDTO::getId).collect(Collectors.toList());
    }
    
    private SharedRideJoinApproval createApproval(UUID employeeId, Status status) {
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var approval = new SharedRideJoinApproval();
        approval.setActionId(UUID.randomUUID());
        approval.setCreationTime(LocalDateTime.now().minusDays(10));
        approval.setAuthorId(UUID.randomUUID());
        approval.setStatus(status);
        approval.setActorId(employeeId);
        approval.setTransportType("PERSONAL");
        approval.setDesiredDate(LocalDateTime.now());
        approval.setPurposeId(tripPurpose.getId());
        return approvalRepository.save(approval);
    }
    
    private UUID user(UUID employeeId) {
        UUID userId = employeeRepository.findById(employeeId).orElseThrow().getUserId();
        Objects.requireNonNull(userId);
        return userId;
    }
    
    
    private Employee createEmployee() {
        var organization = organizationRepository.save(Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .create());
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        return employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
    }
    
    private Collection<? extends TripApproveDTO> getActiveApprovalsForUser(UUID employeeId) {
        return approveService.getActiveApprovalsForUser(user(employeeId));
    }
    
    private Collection<? extends TripApproveDTO> getClosedApprovalsForUser(UUID employeeId) {
        return approveService.getClosedApprovalsForUser(user(employeeId));
    }
    
}
