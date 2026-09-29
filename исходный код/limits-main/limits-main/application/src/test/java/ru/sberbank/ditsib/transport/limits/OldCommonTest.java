package ru.sberbank.ditsib.transport.limits;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
public abstract class OldCommonTest {

    @Autowired
    protected EmployeeRepository employeeRepository;
    @Autowired
    protected OrganizationRepository organizationRepository;
    @Autowired
    protected DepLimitRepository depLimitRepository;
    @Autowired
    protected EmpLimitRepository empLimitRepository;
    @Autowired
    protected LimitSharingRepository limitSharingRepository;
    @Autowired
    protected LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;
    @Autowired
    protected LimitSpendingRepository limitSpendingRepository;
    @Autowired
    protected DepartmentRepository departmentRepository;

    public static final String HUMAN_READABLE_EMPLOYEE_ID_1 = "US-0001-1";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_2 = "US-0001-2";

    public static final String ORGANIZATION_ID = "f10b775b-51db-4e1c-a747-222296041234";
    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String USER2_ID = "558d39f2-c638-490f-90e9-94d896a65b4c";
    public static final String DEPARTMENT_ID = "f3fe6566-56c6-11eb-ae93-0242ac130002";

    public static final UUID userId1 = UUID.fromString(USER1_ID);
    public static final UUID userId2 = UUID.fromString(USER2_ID);
    public static final UUID departmentId = UUID.fromString(DEPARTMENT_ID);
    public static final UUID organizationId = UUID.fromString(ORGANIZATION_ID);

    protected Organization organization1;
    protected Department departmentMain;
    protected Department departmentChild1;
    protected Department departmentChild2;
    protected Department departmentChild1OfChild1;
    protected Employee testEmployee1;
    protected Employee testEmployee2;


    protected LimitSharingPerPeriod createLimitSharingPerPeriod() {
        var lspp = LimitSharingPerPeriod.builder()
            .id(UUID.randomUUID())
            .limitSharing(createLimitSharing())
            .periodData(PeriodData.valueOf(LocalDate.now().getMonth().name()))
            .creationTime(LocalDateTime.now())
            .balance(BigDecimal.valueOf(22222))
            .sum(BigDecimal.valueOf(22222))
            .author(employeeRepository.getReferenceById(userId1))
            .build();
        return limitSharingPerPeriodRepository.save(lspp);
    }

    protected LimitSharing createLimitSharing() {
        return createLimitSharing(LimitSharingType.MONTHLY, true);
    }

    protected LimitSharing createLimitSharing(LimitSharingType limitSharingType) {
        return createLimitSharing(limitSharingType, true);
    }

    protected LimitSharing createLimitSharing(boolean distributed) {
        return createLimitSharing(LimitSharingType.MONTHLY, distributed);
    }

    protected LimitSharing createLimitSharing(LimitSharingType limitSharingType, boolean distributed) {
        DepLimit limit = createDepLimit(limitSharingType);
        LimitSharing limitSharing = new LimitSharing();
        limitSharing.setAuthor(limit.getAuthor());
        limitSharing.setLimit(limit);
        limitSharing.setId(UUID.randomUUID());
        limitSharing.setBalance(BigDecimal.valueOf(22222));
        limitSharing.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitSharing.setDistributed(distributed);
        limitSharing.setSum(BigDecimal.valueOf(55555));
        limitSharing.setTransportType(TransportTypeEnum.TAXI);
        return limitSharingRepository.save(limitSharing);
    }

    protected DepLimit createDepLimit() {
        return createDepLimit(LimitSharingType.MONTHLY);
    }

    protected DepLimit createDepLimit(LimitSharingType limitSharingType) {
        Employee employee = createEmployee();
        DepLimit limit = new DepLimit();
        limit.setDepartment(createDepartment());
        limit.setId(UUID.randomUUID());
        limit.setOrganization(createOrganization());
        limit.setLimitType(LimitType.DEPARTMENT);
        limit.setSum(BigDecimal.valueOf(100));
        limit.setAuthor(employee);
        limit.setLimitOwner(employee);
        limit.setLimitStatus(LimitStatus.SHARED);
        limit.setYear(LocalDate.now().getYear());
        limit.setHumanReadableId("HRI");
        limit.setLimitSharingType(limitSharingType);
        limit.setLimitServiceType("PASSENGER");
        limit.setFinalSharing(true);
        limit.setUseThisLimit(true);
        limit.setCreationTime(LocalDateTime.now());
        return depLimitRepository.save(limit);
    }

    protected EmpLimit createEmpLimit() {
        Employee employee = createEmployee();
        EmpLimit limit = new EmpLimit();
        limit.setId(UUID.randomUUID());
        limit.setEmployee(employee);
        limit.setOrganization(createOrganization());
        limit.setLimitType(LimitType.EMPLOYEE);
        limit.setSum(BigDecimal.valueOf(100));
        limit.setAuthor(employee);
        limit.setLimitOwner(employee);
        limit.setLimitStatus(LimitStatus.SHARED);
        limit.setYear(LocalDate.now().getYear());
        limit.setHumanReadableId("HRI");
        limit.setLimitServiceType("PASSENGER");
        limit.setLimitSharingType(LimitSharingType.MONTHLY);
        limit.setFinalSharing(true);
        limit.setUseThisLimit(true);
        limit.setCreationTime(LocalDateTime.now());
        return empLimitRepository.save(limit);
    }

    protected Employee createEmployee() {
        Employee employee = Employee.builder().id(userId1).userId(userId1)
            .departmentId(departmentId).build();
        return employeeRepository.save(employee);
    }

    protected Department createDepartment() {
        var dep =
            Department.builder().id(departmentId).departmentName("name").organizationId(createOrganization().getId()).active(true)
                .humanReadableId("dep").build();
        return departmentRepository.save(dep);
    }

    protected Organization createOrganization() {
        Organization organization = Organization.builder().id(organizationId).digitId(999L).build();
        return organizationRepository.save(organization);
    }

    protected void assertLimit(Limit actual, Limit expected) {
        assertThat(actual.getAuthor().getId()).isEqualTo(expected.getAuthor().getId());
        assertThat(actual.getLimitOwner().getId()).isEqualTo(expected.getLimitOwner().getId());
        assertThat(actual.getSum()).isEqualTo(expected.getSum());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getLimitType().getName()).isEqualTo(expected.getLimitType().getName());
        assertThat(actual.getOrganization().getId()).isEqualTo(expected.getOrganization().getId());
    }

    protected void assertLimitSharingWithoutSumBalance(LimitSharing actual, LimitSharing expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getAuthor().getId()).isEqualTo(expected.getLimit().getAuthor().getId());
        assertThat(actual.getLimit().getId()).isEqualTo(expected.getLimit().getId());
        assertThat(actual.getTransportType().name()).isEqualTo(expected.getTransportType().name());
        assertThat(actual.isDistributed()).isEqualTo(expected.isDistributed());
    }
}
