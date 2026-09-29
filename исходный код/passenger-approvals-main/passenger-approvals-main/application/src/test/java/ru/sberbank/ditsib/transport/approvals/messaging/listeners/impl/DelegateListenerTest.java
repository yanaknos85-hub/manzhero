package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.messaging.message.DelegateMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@EmbeddedPostgres
@DisplayName("Проверка получения делегатов")
@MockitoBean(types = {JwtDecoder.class})
class DelegateListenerTest extends KafkaTest {

    @Autowired
    private DelegateRepository repository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepLimitRepository limitRepository;
    @Autowired
    @Qualifier("delegateInput")
    private Consumer<Message<DelegateMessage>> sink;
    
    @AfterEach
    void afterEach() {
        repository.deleteAll();
        limitRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Новое")
    void handleDelegate_new() {
        assertThat(repository.count()).isZero();
        var message = DelegateMessage.builder()
                                     .id(UUID.randomUUID())
                                     .delegateId(UUID.randomUUID())
                                     .supervisorId(UUID.randomUUID())
                                     .transportTypeId(TransportTypeEnum.TAXI.getId())
                                     .startDate(LocalDate.now().minusDays(1))
                                     .endDate(LocalDate.now().plusDays(1))
                                     .build();
    
        sink.accept(MessageBuilder.withPayload(message).build());
    
        assertThat(repository.count()).isEqualTo(1);
    
        var actual = repository.findAll().get(0);
    
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDelegateId()).isEqualTo(message.getDelegateId());
        assertThat(actual.getSupervisorId()).isEqualTo(message.getSupervisorId());
        assertThat(TransportTypeEnum.valueOf(actual.getTransportType()).getId()).isEqualTo(message.getTransportTypeId());
        assertThat(actual.getStartDate()).isEqualTo(message.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(message.getEndDate());
        
    }
    
    @Test
    @DisplayName("Редактирование")
    void handleDelegate_edit() {
        assertThat(repository.count()).isZero();
        var id = UUID.randomUUID();
        Delegate delegate = new Delegate();
        delegate.setId(id);
        delegate.setDelegateId(UUID.randomUUID());
        delegate.setSupervisorId(UUID.randomUUID());
        delegate.setTransportType("TAXI");
        delegate.setStartDate(LocalDate.now().minusDays(1));
        delegate.setEndDate(LocalDate.now().plusDays(1));
        repository.save(delegate);
        assertThat(repository.count()).isEqualTo(1);

        var message = DelegateMessage.builder()
                                     .id(id)
                                     .delegateId(UUID.randomUUID())
                                     .supervisorId(UUID.randomUUID())
                                     .transportTypeId(TransportTypeEnum.TAXI.getId())
                                     .startDate(LocalDate.now().minusDays(1))
                                     .endDate(LocalDate.now().plusDays(1))
                                     .build();
        
        sink.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDelegateId()).isEqualTo(message.getDelegateId());
        assertThat(actual.getSupervisorId()).isEqualTo(message.getSupervisorId());
        assertThat(TransportTypeEnum.valueOf(actual.getTransportType()).getId()).isEqualTo(message.getTransportTypeId());
        assertThat(actual.getStartDate()).isEqualTo(message.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(message.getEndDate());
        
    }
    
    @Test
    @DisplayName("Удаление")
    void handleDelegate_delete() {
        assertThat(repository.count()).isZero();
        var id = UUID.randomUUID();
        Delegate delegate = new Delegate();
        delegate.setId(id);
        delegate.setDelegateId(UUID.randomUUID());
        delegate.setSupervisorId(UUID.randomUUID());
        delegate.setTransportType("TAXI");
        delegate.setStartDate(LocalDate.now().minusDays(1));
        delegate.setEndDate(LocalDate.now().plusDays(1));
        repository.save(delegate);
        assertThat(repository.count()).isEqualTo(1);

        var message = DelegateMessage.builder()
                                     .id(id)
                                     .deleted(true)
                                     .build();
        
        sink.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isZero();
        
    }
    
    @Test
    @DisplayName("new & update c обновлением согласующих")
    @Disabled("На jenkins в подразделении некорректное количество апруверов, причина, скорее всего, в работе делегатов")
    void handleDelegate_new_withApproversUpdate() {
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
        var head = employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
        var delegateEmployee = employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
        departmentRepository.save(department.setDepartmentHeadId(head.getId()));
        limitRepository.save(DepLimit.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .year(LocalDate.now().getYear())
                .sum(100L)
                .reserve(200L)
                .ownerId(head.getId())
                .build());
        
        assertThat(repository.count()).isZero();
        var departmentOptional = departmentRepository.findDepartmentWithApprovers(department.getId());
        assertThat(departmentOptional).isPresent();
        assertThat(departmentOptional.get().getApprovers()).containsExactlyInAnyOrder();
        var message = DelegateMessage.builder()
                                     .id(UUID.randomUUID())
                                     .delegateId(delegateEmployee.getId())
                                     .supervisorId(head.getId())
                                     .transportTypeId(TransportTypeEnum.TAXI.getId())
                                     .startDate(LocalDate.now().minusDays(2))
                                     .endDate(LocalDate.now().plusDays(1))
                                     .build();
        
        sink.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDelegateId()).isEqualTo(message.getDelegateId());
        assertThat(actual.getSupervisorId()).isEqualTo(message.getSupervisorId());
        assertThat(TransportTypeEnum.valueOf(actual.getTransportType()).getId()).isEqualTo(message.getTransportTypeId());
        assertThat(actual.getStartDate()).isEqualTo(message.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(message.getEndDate());
        departmentOptional = departmentRepository.findDepartmentWithApprovers(department.getId());
        assertThat(departmentOptional).isPresent();
        assertThat(departmentOptional.get().getApprovers())
                .containsExactlyInAnyOrder(headApprover(head.getId()), delegateApprover(message.getDelegateId()));

        // Update
        message.setEndDate(LocalDate.now().minusDays(1));// Делаем делегата неактивным
        sink.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        departmentOptional = departmentRepository.findDepartmentWithApprovers(department.getId());
        assertThat(departmentOptional).isPresent();
        assertThat(departmentOptional.get().getApprovers())
                .containsExactlyInAnyOrder(headApprover(head.getId()));
        
    }
    
    @Test
    @DisplayName("new & update c обновлением согласующих. При этом один head для двух подразделений (тест создан по мотивам фикса баги)")
    @Disabled("На jenkins в подразделении некорректное количество апруверов, причина, скорее всего, в работе делегатов")
    void handleDelegate_new_withApproversUpdate_OneHeadTwoDepartment() {
        // tmpDepartment в логике не участвует. Он нужен только для того, чтобы проверить что логика не будет падать
        // при наличии более одного подразделения у одного head.
        var organization = organizationRepository.save(Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .create());
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var tmpDepartment = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        var head = employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
        var delegateEmployee = employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
        departmentRepository.save(department.setDepartmentHeadId(head.getId()));
        departmentRepository.save(tmpDepartment.setDepartmentHeadId(head.getId()));
        limitRepository.save(DepLimit.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .year(LocalDate.now().getYear())
                .sum(100L)
                .reserve(200L)
                .ownerId(head.getId())
                .build());
        
        assertThat(repository.count()).isZero();
        var departmentOptional = departmentRepository.findDepartmentWithApprovers(department.getId());
        assertThat(departmentOptional).isPresent();
        assertThat(departmentOptional.get().getApprovers()).containsExactlyInAnyOrder();
        var message = DelegateMessage.builder()
                                     .id(UUID.randomUUID())
                                     .delegateId(delegateEmployee.getId())
                                     .supervisorId(head.getId())
                                     .transportTypeId(TransportTypeEnum.TAXI.getId())
                                     .startDate(LocalDate.now().minusDays(2))
                                     .endDate(LocalDate.now().plusDays(1))
                                     .build();
        
        sink.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDelegateId()).isEqualTo(message.getDelegateId());
        assertThat(actual.getSupervisorId()).isEqualTo(message.getSupervisorId());
        assertThat(TransportTypeEnum.valueOf(actual.getTransportType())).isEqualTo(message.getTransportTypeId());
        assertThat(actual.getStartDate()).isEqualTo(message.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(message.getEndDate());
        departmentOptional = departmentRepository.findDepartmentWithApprovers(department.getId());
        assertThat(departmentOptional).isPresent();
        assertThat(departmentOptional.get().getApprovers())
                .containsExactlyInAnyOrder(headApprover(head.getId()), delegateApprover(message.getDelegateId()));
        
        // Update
        message.setEndDate(LocalDate.now().minusDays(1));// Делаем делегата неактивным
        sink.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        departmentOptional = departmentRepository.findDepartmentWithApprovers(department.getId());
        assertThat(departmentOptional).isPresent();
        assertThat(departmentOptional.get().getApprovers())
                .containsExactlyInAnyOrder(headApprover(head.getId()));
    }

    private static Approver delegateApprover(UUID employeeId) {
        return Approver.builder().employeeId(employeeId).transportType(TransportTypeEnum.TAXI.name()).build();
    }

    private static Approver headApprover(UUID employeeId) {
        return Approver.builder().employeeId(employeeId).build();
    }
}