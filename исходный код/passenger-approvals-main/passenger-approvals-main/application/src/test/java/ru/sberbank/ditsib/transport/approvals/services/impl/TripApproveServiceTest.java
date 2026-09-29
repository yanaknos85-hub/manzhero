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
import ru.sberbank.ditsib.transport.approvals.services.TripApproverService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@DisplayName("Проверка сервиса согласования заявок на поездки")
@SpringBootTest(classes = ApprovalsApplication.class)
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
class TripApproveServiceTest extends KafkaTest {
    @Autowired
    private ApproveService<TripRequestApproval> approveService;
    
    @Autowired
    private TripRequestApprovalRepository approvalRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DelegateRepository delegateRepository;
    
    @Autowired
    private DepLimitRepository limitRepository;
    
    @Autowired
    private TripApproverService approverService;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    
    private Department department_1;
    private Department department_1_1;
    private Department department_1_2;
    private Department department_1_3;
    private Department department_1_1_1;
    private Department department_1_2_1;
    private Delegate delegate_1_1;
    private Delegate delegate_1_2;
    private Delegate delegate_1_1_1;
    private Delegate delegate_1_3_1;
    private Delegate delegate_1_1_1_1;
    private Delegate delegate_1_2_1_1;

    @BeforeEach
    public void beforeEach() {
        // Initial Scene
        var organization = organizationRepository.save(Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .create());
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        // Top department
        department_1 = createDepartment(null, organization.getId(), position.getId());
        
        // Childs of department 1
        department_1_1 = createDepartment(department_1, organization.getId(), position.getId());
        department_1_2 = createDepartment(department_1, organization.getId(), position.getId());
        department_1_3 = createDepartment(department_1, organization.getId(), position.getId());
        
        department_1_1_1 = createDepartment(department_1_1, organization.getId(), position.getId());
        department_1_2_1 = createDepartment(department_1_2, organization.getId(), position.getId());
        
        createLimit(department_1);
        createLimit(department_1_2);
        createLimit(department_1_3);
        
        delegate_1_1 = createDelegate(department_1, position.getId());
        delegate_1_2 = createDelegate(department_1_2, position.getId());
        createDelegate(department_1_3, position.getId());
        
        delegate_1_1_1 = createDelegate(department_1_1, position.getId());
        createDelegate(department_1_2, position.getId());
        delegate_1_3_1 = createDelegate(department_1_3, position.getId());
        delegate_1_1_1_1 = createDelegate(department_1_1_1, position.getId());
        delegate_1_2_1_1 = createDelegate(department_1_2_1, position.getId());
        
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
        limitRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Проверка получения пустого списка согласований")
    void test_empty() {
        var approvals = getActiveApprovalsForUser(department_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(0);
        approvals = getActiveApprovalsForUser(department_1_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(0);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(0);
        approvals = getActiveApprovalsForUser(department_1_3.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(0);
        approvals = getActiveApprovalsForUser(department_1_1_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(0);
        approvals = getActiveApprovalsForUser(department_1_2_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Проверка получения списка согласований")
    void testGetActive() {
        // ************* Create approvals ***************
        var approval_1_1 = createApproval(department_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_2 = createApproval(department_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_1_1 = createApproval(department_1_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_1_2 = createApproval(department_1_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_1.getDepartmentHeadId(), Status.APPROVED);
    
        var approval_1_2_1 = createApproval(department_1_2.getDepartmentHeadId(), Status.NEW);
        var approval_1_2_2 = createApproval(department_1_2.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_2.getDepartmentHeadId(), Status.APPROVED);
    
        var approval_1_3_1 = createApproval(department_1_3.getDepartmentHeadId(), Status.NEW);
        var approval_1_3_2 = createApproval(department_1_3.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_3.getDepartmentHeadId(), Status.APPROVED);
    
        var approval_1_2_1_1 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_2_1_2 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_2_1.getDepartmentHeadId(), Status.APPROVED);

        var approval_1_1_1_1 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_1_1_2 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_1_1.getDepartmentHeadId(), Status.APPROVED);
    
        assertThat(approvalRepository.count()).isEqualTo(6 * 3);
    
        // ************* Check get approvals ***************

        // department_1_1_1 has 2 active approvals but not limit owner. Result 2 approvals for head user
        var approvals = getActiveApprovalsForUser(department_1_1_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);

        // department_1_1_1 has 2 active approvals but not limit owner. Result 2 approvals for delegate user
        approvals = getActiveApprovalsForUser(delegate_1_1_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
    
        // department_1_2_1 has 2 active approvals but not limit owner. Result 2 approvals for head user
        approvals = getActiveApprovalsForUser(department_1_2_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
    
        // department_1_2_1 has 2 active approvals but not limit owner. Result 2 approvals for delegate user
        approvals = getActiveApprovalsForUser(delegate_1_2_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
    
        // department_1_1 has 2 active approvals but not limit owner. Result 2 approvals for head user
        approvals = getActiveApprovalsForUser(department_1_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
    
        // department_1_1 has 2 active approvals but not limit owner. Result 2 approvals for delegate user
        approvals = getActiveApprovalsForUser(delegate_1_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
    
        // department_1_2 has 2 active approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1_2 + 0 from department_1_2_1
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
    
        // department_1_2 has 2 active approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1_2 + 0 from department_1_2_1
        approvals = getActiveApprovalsForUser(delegate_1_2.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
    
        // department_1_3 has 2 active approvals and limit owner. Result 2 approvals for head user
        approvals = getActiveApprovalsForUser(department_1_3.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
    
        // department_1_3 has 2 active approvals and limit owner. Result 2 approvals for delegate user
        approvals = getActiveApprovalsForUser(delegate_1_3_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
    
        // department_1 has 2 active approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getActiveApprovalsForUser(department_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    
        // department_1 has 2 active approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getActiveApprovalsForUser(delegate_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    }
    
    @Test
    @DisplayName("Проверка получения списка неактивных согласований")
    void testGetClosed() {
        // ************* Create approvals ***************
        var approval_1_1 = createApproval(department_1.getDepartmentHeadId(), Status.APPROVED);
        var approval_1_2 = createApproval(department_1.getDepartmentHeadId(), Status.APPROVED);
        createApproval(department_1.getDepartmentHeadId(), Status.EDITED);
        
        var approval_1_1_1 = createApproval(department_1_1.getDepartmentHeadId(), Status.APPROVED);
        var approval_1_1_2 = createApproval(department_1_1.getDepartmentHeadId(), Status.APPROVED);
        createApproval(department_1_1.getDepartmentHeadId(), Status.NEW);
        
        var approval_1_2_1 = createApproval(department_1_2.getDepartmentHeadId(), Status.APPROVED);
        var approval_1_2_2 = createApproval(department_1_2.getDepartmentHeadId(), Status.APPROVED);
        createApproval(department_1_2.getDepartmentHeadId(), Status.EDITED);
        
        var approval_1_3_1 = createApproval(department_1_3.getDepartmentHeadId(), Status.APPROVED);
        var approval_1_3_2 = createApproval(department_1_3.getDepartmentHeadId(), Status.APPROVED);
        createApproval(department_1_3.getDepartmentHeadId(), Status.NEW);
        
        var approval_1_2_1_1 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.APPROVED);
        var approval_1_2_1_2 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.APPROVED);
        createApproval(department_1_2_1.getDepartmentHeadId(), Status.EDITED);
        
        var approval_1_1_1_1 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.APPROVED);
        var approval_1_1_1_2 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.APPROVED);
        createApproval(department_1_1_1.getDepartmentHeadId(), Status.NEW);
        
        assertThat(approvalRepository.count()).isEqualTo(6 * 3);
        
        // ************* Check get approvals ***************
        
        // department_1_1_1 has 2 closed approvals but not limit owner. Result 2 approvals for head user
        var approvals =
                getClosedApprovalsForUser(department_1_1_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        
        // department_1_1_1 has 2 closed approvals but not limit owner. Result 2 approvals for delegate user
        approvals = getClosedApprovalsForUser(delegate_1_1_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        
        // department_1_2_1 has 2 closed approvals but not limit owner. Result 2 approvals for head user
        approvals =getClosedApprovalsForUser(department_1_2_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        
        // department_1_2_1 has 2 closed approvals but not limit owner. Result 2 approvals for delegate user
        approvals = getClosedApprovalsForUser(delegate_1_2_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        
        // department_1_1 has 2 closed approvals but not limit owner. Result 2 approvals for head user
        approvals = getClosedApprovalsForUser(department_1_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        
        // department_1_1 has 2 closed approvals but not limit owner. Result 2 approvals for delegate user
        approvals = getClosedApprovalsForUser(delegate_1_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        
        // department_1_2 has 2 closed approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1_2 + 0 from department_1_2_1
        approvals = getClosedApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
        
        // department_1_2 has 2 closed approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1_2 + 0 from department_1_2_1
        approvals = getClosedApprovalsForUser(delegate_1_2.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
        
        // department_1_3 has 2 closed approvals and limit owner. Result 2 approvals for head user
        approvals = getClosedApprovalsForUser(department_1_3.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
        
        // department_1_3 has 2 closed approvals and limit owner. Result 2 approvals for delegate user
        approvals = getClosedApprovalsForUser(delegate_1_3_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
        
        // department_1 has 2 closed approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getClosedApprovalsForUser(department_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
        
        // department_1 has 2 closed approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getClosedApprovalsForUser(delegate_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    }
    
    @Test
    @DisplayName("Список согласований пустой из-за просроченного лимита")
    void testGetWithOutDateLimit() {
        // ************* Create approvals ***************
        var approval_1_1 = createApproval(department_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_2 = createApproval(department_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_1_1 = createApproval(department_1_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_1_2 = createApproval(department_1_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_2_1 = createApproval(department_1_2.getDepartmentHeadId(), Status.NEW);
        var approval_1_2_2 = createApproval(department_1_2.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_2.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_3_1 = createApproval(department_1_3.getDepartmentHeadId(), Status.NEW);
        var approval_1_3_2 = createApproval(department_1_3.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_3.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_2_1_1 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_2_1_2 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_2_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_1_1_1 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_1_1_2 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_1_1.getDepartmentHeadId(), Status.APPROVED);
        
        assertThat(approvalRepository.count()).isEqualTo(6 * 3);
        
        // ************* Check get approvals ***************
        
        // department_1_2 has 2 active approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1_2 + 0 from department_1_2_1
        var approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();

        updateLimitYear(2000);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateLimitYear(LocalDate.now().getYear());
        
        // department_1_2 has 2 active approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1_2 + 0 from department_1_2_1
        approvals = getActiveApprovalsForUser(delegate_1_2.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
    
        updateLimitYear(2000);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateLimitYear(LocalDate.now().getYear());
        
        // department_1_3 has 2 active approvals and limit owner. Result 2 approvals for head user
        approvals = getActiveApprovalsForUser(department_1_3.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
    
        updateLimitYear(2000);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateLimitYear(LocalDate.now().getYear());

        // department_1_3 has 2 active approvals and limit owner. Result 2 approvals for delegate user
        approvals = getActiveApprovalsForUser(delegate_1_3_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();

        updateLimitYear(2000);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateLimitYear(LocalDate.now().getYear());
        
        // department_1 has 2 active approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getActiveApprovalsForUser(department_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    
        updateLimitYear(2000);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateLimitYear(LocalDate.now().getYear());
    
        // department_1 has 2 active approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getActiveApprovalsForUser(delegate_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    
        updateLimitYear(2000);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateLimitYear(LocalDate.now().getYear());
    }
    
    @Test
    @DisplayName("Список согласований пустой из-за удаленного лимита")
    public void testGetWithDeletedLimit() {
        // ************* Create approvals ***************
        var approval_1_1 = createApproval(department_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_2 = createApproval(department_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_1_1 = createApproval(department_1_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_1_2 = createApproval(department_1_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_2_1 = createApproval(department_1_2.getDepartmentHeadId(), Status.NEW);
        var approval_1_2_2 = createApproval(department_1_2.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_2.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_3_1 = createApproval(department_1_3.getDepartmentHeadId(), Status.NEW);
        var approval_1_3_2 = createApproval(department_1_3.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_3.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_2_1_1 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_2_1_2 = createApproval(department_1_2_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_2_1.getDepartmentHeadId(), Status.APPROVED);
        
        var approval_1_1_1_1 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.NEW);
        var approval_1_1_1_2 = createApproval(department_1_1_1.getDepartmentHeadId(), Status.EDITED);
        createApproval(department_1_1_1.getDepartmentHeadId(), Status.APPROVED);
        
        assertThat(approvalRepository.count()).isEqualTo(6 * 3);
        
        // ************* Check get approvals ***************
        
        // department_1_2 has 2 active approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1_2 + 0 from department_1_2_1
        var approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
        
        updateDeleted(true);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateDeleted(false);
        
        // department_1_2 has 2 active approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1_2 + 0 from department_1_2_1
        approvals = getActiveApprovalsForUser(delegate_1_2.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_2_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_2_1_2.getId())).isFalse();
    
        updateDeleted(true);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateDeleted(false);
        
        // department_1_3 has 2 active approvals and limit owner. Result 2 approvals for head user
        approvals = getActiveApprovalsForUser(department_1_3.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
    
        updateDeleted(true);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateDeleted(false);
        
        // department_1_3 has 2 active approvals and limit owner. Result 2 approvals for delegate user
        approvals = getActiveApprovalsForUser(delegate_1_3_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_3_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_3_2.getId())).isTrue();
    
        updateDeleted(true);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateDeleted(false);
        
        // department_1 has 2 active approvals and limit owner.
        // Result 2 approvals for head user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getActiveApprovalsForUser(department_1.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    
        updateDeleted(true);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateDeleted(false);
        
        // department_1 has 2 active approvals and limit owner.
        // Result 2 approvals for delegate user: 2 from department_1 + 0 from department_1_1 + 0 from department_1_1_1
        approvals = getActiveApprovalsForUser(delegate_1_1.getDelegateId());
        assertThat(approvals.size()).isEqualTo(2);
        assertThat(toIds(approvals).contains(approval_1_1.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_2.getId())).isTrue();
        assertThat(toIds(approvals).contains(approval_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_2.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_1.getId())).isFalse();
        assertThat(toIds(approvals).contains(approval_1_1_1_2.getId())).isFalse();
    
        updateDeleted(true);
        approvals = getActiveApprovalsForUser(department_1_2.getDepartmentHeadId());
        assertThat(approvals.size()).isEqualTo(2);
        updateDeleted(false);
    }
    
    @Test
    @DisplayName("Удаление дубликатов")
    public void testRemoveDuplicates() {
        var t1 = new TripRequestApproval();
        t1.setId(UUID.randomUUID());
        var t2 = new TripRequestApproval();
        t2.setId(UUID.randomUUID());
        var t3 = new TripRequestApproval();
        t3.setId(t2.getId());
        List<UUID> ids =
                ApproveService.removeDuplicates(Arrays.asList(t1, t2, t3)).stream()
                                             .map(TripRequestApproval::getId)
                                             .collect(Collectors.toList());
        assertThat(ids.size()).isEqualTo(2);
        assertThat(ids.contains(t1.getId())).isTrue();
        assertThat(ids.contains(t2.getId())).isTrue();
    
        UUID approval_1_id = createApproval(department_1.getDepartmentHeadId(), Status.NEW).getId();
        UUID approval_2_id = createApproval(department_1.getDepartmentHeadId(), Status.NEW).getId();
    
        var approval11 = approvalRepository.findById(approval_1_id).orElseThrow();
        var approval12 = approvalRepository.findById(approval_1_id).orElseThrow();
        var approval2 = approvalRepository.findById(approval_2_id).orElseThrow();
    
        ids = ApproveService.removeDuplicates(Arrays.asList(approval11, approval12, approval2)).stream()
                                           .map(TripRequestApproval::getId)
                                           .collect(Collectors.toList());
        assertThat(ids.size()).isEqualTo(2);
        assertThat(ids.contains(approval_1_id)).isTrue();
        assertThat(ids.contains(approval_2_id)).isTrue();
    
    }
    
    private void updateLimitYear(int year) {
        limitRepository.findAll().forEach(l -> {
            l.setYear(year);
             limitRepository.save(l);
        });
    }
    
    private void updateDeleted(boolean deleted) {
        limitRepository.findAll().forEach(l -> {
            l.setDeleted(deleted);
            limitRepository.save(l);
        });
    }
    
    private List<UUID> toIds(Collection<? extends TripApproveDTO> approvals) {
        return approvals.stream().map(TripApproveDTO::getId).collect(Collectors.toList());
    }
    
    private TripRequestApproval createApproval(UUID employeeId, Status status) {
        var approval = new TripRequestApproval();
        approval.setActionId(UUID.randomUUID());
        approval.setCreationTime(LocalDateTime.now().minusDays(10));
        approval.setAuthorId(UUID.randomUUID());
        approval.setStatus(status);
        approval.setActorId(employeeId);
        approval.setTransportType("TAXI");
        approvalRepository.save(approval);
        return approval;
    }
    
    private UUID user(UUID employeeId) {
        UUID userId = employeeRepository.findById(employeeId).orElseThrow().getUserId();
        Objects.requireNonNull(userId);
        return userId;
    }


    private Department createDepartment(Department parent, UUID organizationId, UUID positionId) {
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organizationId)
                .set(field(Department::getParentId), parent == null ? null : parent.getId())
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        var employee = createEmployee(department.getId(), positionId);
        departmentRepository.save(department.setDepartmentHeadId(employee.getId()));
        return department;
    }

    private Employee createEmployee(UUID departmentId, UUID positionId) {
        return employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), positionId)
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
    }

    private void createLimit(Department department) {
        var limit = new DepLimit();
        limit.setId(UUID.randomUUID());
        limit.setOwnerId(department.getDepartmentHeadId());
        limit.setParentId(UUID.randomUUID());
        limit.setYear(LocalDate.now().getYear());
        limit.setSum(100L);
        limit.setReserve(200L);
        limitRepository.save(limit);
    }

    private Delegate createDelegate(Department department, UUID positionId) {
        var employee = createEmployee(department.getId(), positionId);

        var delegate = new Delegate();
        delegate.setId(UUID.randomUUID());
        delegate.setDelegateId(employee.getId());
        delegate.setSupervisorId(department.getDepartmentHeadId());
        delegate.setTransportType("TAXI");
        delegate.setStartDate(LocalDate.now().minusDays(1));
        delegate.setEndDate(LocalDate.now().plusDays(1));
        delegateRepository.save(delegate);
        return delegate;
    }
    
    private void updateApproversForAllDepartments() {
        departmentRepository.findAll().forEach(dep -> approverService.onDepartmentChanged(dep.getId()));
    }
    
    private Collection<? extends TripApproveDTO> getActiveApprovalsForUser(UUID employeeId) {
        updateApproversForAllDepartments();
        return approveService.getActiveApprovalsForUser(user(employeeId));
    }
    
    private Collection<? extends TripApproveDTO> getClosedApprovalsForUser(UUID employeeId) {
        updateApproversForAllDepartments();
        return approveService.getClosedApprovalsForUser(user(employeeId));
    }
    
}
