package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitNotSufficientException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitHistoryService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса лимитов")
class LimitServiceImplTest {

    private final EmpLimitRepository empLimitRepository = mock(EmpLimitRepository.class);

    private final DepLimitRepository depLimitRepository = mock(DepLimitRepository.class);

    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);

    @SuppressWarnings("unchecked")
    private final LimitRepository<Limit> limitRepository = mock(LimitRepository.class);

    private final LimitSharingService limitSharingService = mock(LimitSharingService.class);

    private final LimitTransferHistoryService limitTransferHistoryService = mock(LimitTransferHistoryService.class);

    private final LimitHistoryService limitHistoryService = mock(LimitHistoryService.class);

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

    private final LimitService service = new LimitServiceImpl(limitRepository, limitSharingService, empLimitRepository,
            depLimitRepository, limitTransferHistoryService, limitHistoryService, departmentRepository, employeeRepository);

    @Test
    @DisplayName("Есть личный лимит и он больше лимита подразделения")
    void getLimiInfoByDate1() {
        var date = LocalDate.of(2023, 2, 2);
        UUID id = UUID.randomUUID();
        var emp = Optional.of(Employee.builder().id(id)
                .humanReadableId("TEST")
                .departmentId(id)
                .organizationId(id).build());

        EmpLimit empLimit = new EmpLimit();
        empLimit.setLimitSharingType(LimitSharingType.MONTHLY);
        empLimit.setEmployee(emp.get());
        var limitSharing = LimitSharing.builder().limit(empLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriod = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(100111))
                .balance(BigDecimal.valueOf(99999))
                .limitSharing(limitSharing).build();
        limitSharing.getSharingPerPeriods().add(limitSharingByPeriod);
        List<EmpLimit> empLimitList = Collections.singletonList(empLimit);
        empLimit.setUseThisLimit(false);
        empLimit.getSharings().add(limitSharing);
        doReturn(empLimitList)
                .when(empLimitRepository)
                .findByEmployeeIdAndYearAndLimitStatusAndLimitType(emp.get().getId()
                        , 2023, LimitStatus.SHARED, LimitType.EMPLOYEE);
        DepLimit depLimit = new DepLimit();
        depLimit.setDepartment(Department.builder().id(id).build());
        depLimit.setYear(2023);
        depLimit.setLimitType(LimitType.DEPARTMENT);
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);

        var limitSharingDep = LimitSharing.builder().limit(depLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriodDep = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(100))
                .balance(BigDecimal.valueOf(9))
                .limitSharing(limitSharingDep).build();
        limitSharingDep.getSharingPerPeriods().add(limitSharingByPeriodDep);
        List<DepLimit> depLimitList = Collections.singletonList(depLimit);
        depLimit.setUseThisLimit(false);
        depLimit.getSharings().add(limitSharingDep);
        doReturn(depLimitList)
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitStatusAndLimitType(emp.get().getDepartmentId()
                        , 2023, LimitStatus.SHARED, LimitType.DEPARTMENT);
        var result = service.getLimiInfoByDate(emp.get(), date, TransportTypeEnum.DEDICATED);
        assertEquals(100111, result.getSum().intValue());
        assertEquals(99999, result.getBalance().intValue());
        assertEquals(2023, result.getYear());
        assertEquals(LimitSharingType.MONTHLY, result.getLimitSharingType());
        assertEquals(TransportTypeEnum.DEDICATED, result.getTransportType());
    }

    @Test
    @DisplayName("Есть личный лимит и лимит подразделения больше личного")
    void getLimiInfoByDate2() {
        var date = LocalDate.of(2023, 2, 2);
        UUID id = UUID.randomUUID();
        var emp = Optional.of(Employee.builder().id(id)
                .humanReadableId("TEST")
                .organizationId(id).build());

        EmpLimit empLimit = new EmpLimit();
        empLimit.setLimitSharingType(LimitSharingType.MONTHLY);
        empLimit.setEmployee(emp.get());
        var limitSharing = LimitSharing.builder().limit(empLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriod = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(100111))
                .balance(BigDecimal.valueOf(99999))
                .limitSharing(limitSharing).build();
        limitSharing.getSharingPerPeriods().add(limitSharingByPeriod);
        List<EmpLimit> empLimitList = Collections.singletonList(empLimit);
        empLimit.setUseThisLimit(true);
        var limitParent = new DepLimit();
        limitParent.setParent(null);
        limitParent.setLimitSharingType(LimitSharingType.MONTHLY);
        var limitSharingParent = LimitSharing.builder().limit(empLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriodParent = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(2000000))
                .balance(BigDecimal.valueOf(299999))
                .limitSharing(limitSharingParent).build();
        limitSharingParent.getSharingPerPeriods().add(limitSharingByPeriodParent);
        limitParent.getSharings().add(limitSharingParent);
        empLimit.setParent(limitParent);
        empLimit.getSharings().add(limitSharing);
        doReturn(empLimitList)
                .when(empLimitRepository)
                .findByEmployeeIdAndYearAndLimitStatusAndLimitType(emp.get().getId()
                        , 2023, LimitStatus.SHARED, LimitType.EMPLOYEE);
        DepLimit depLimit = new DepLimit();
        depLimit.setDepartment(Department.builder().id(id).build());
        depLimit.setYear(2023);
        depLimit.setLimitType(LimitType.DEPARTMENT);
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);

        var limitSharingDep = LimitSharing.builder().limit(depLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriodDep = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(2000000))
                .balance(BigDecimal.valueOf(299999))
                .limitSharing(limitSharingDep).build();
        limitSharingDep.getSharingPerPeriods().add(limitSharingByPeriodDep);
        List<DepLimit> depLimitList = Collections.singletonList(depLimit);
        depLimit.setUseThisLimit(false);
        depLimit.getSharings().add(limitSharingDep);
        doReturn(depLimitList)
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitStatusAndLimitType(emp.get().getDepartmentId()
                        , 2023, LimitStatus.SHARED, LimitType.DEPARTMENT);
        var result = service.getLimiInfoByDate(emp.get(), date, TransportTypeEnum.DEDICATED);
        assertEquals(BigDecimal.valueOf(2000000), result.getSum());
        assertEquals(BigDecimal.valueOf(299999), result.getBalance());
        assertEquals(2023, result.getYear());
        assertEquals(LimitSharingType.MONTHLY, result.getLimitSharingType());
        assertEquals(TransportTypeEnum.DEDICATED, result.getTransportType());
    }

    @Test
    @DisplayName("Нет личного лимита и есть лимит подразделения")
    void getLimiInfoByDate3() {
        var date = LocalDate.of(2023, 2, 2);
        UUID id = UUID.randomUUID();
        var emp = Optional.of(Employee.builder().id(id)
                .humanReadableId("TEST")
                .departmentId(id)
                .organizationId(id).build());

        List<EmpLimit> empLimitList = new ArrayList<>();
        doReturn(empLimitList)
                .when(empLimitRepository)
                .findByEmployeeIdAndYearAndLimitStatusAndLimitType(emp.get().getId()
                        , 2023, LimitStatus.SHARED, LimitType.EMPLOYEE);
        DepLimit depLimit = new DepLimit();
        depLimit.setDepartment(Department.builder().id(id).build());
        depLimit.setYear(2023);
        depLimit.setLimitType(LimitType.DEPARTMENT);
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);

        var limitSharingDep = LimitSharing.builder().limit(depLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriodDep = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(100))
                .balance(BigDecimal.valueOf(9))
                .limitSharing(limitSharingDep).build();
        limitSharingDep.getSharingPerPeriods().add(limitSharingByPeriodDep);
        List<DepLimit> depLimitList = Collections.singletonList(depLimit);
        depLimit.setUseThisLimit(false);
        depLimit.getSharings().add(limitSharingDep);
        doReturn(depLimitList)
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitStatusAndLimitType(emp.get().getDepartmentId()
                        , 2023, LimitStatus.SHARED, LimitType.DEPARTMENT);
        var result = service.getLimiInfoByDate(emp.get(), date, TransportTypeEnum.DEDICATED);
        assertEquals(BigDecimal.valueOf(100), result.getSum());
        assertEquals(BigDecimal.valueOf(9), result.getBalance());
        assertEquals(2023, result.getYear());
        assertEquals(LimitSharingType.MONTHLY, result.getLimitSharingType());
        assertEquals(TransportTypeEnum.DEDICATED, result.getTransportType());
    }

    @Test
    @DisplayName("Нет личного лимита и нет лимита подразделения")
    void getLimiInfoByDate4() {
        var date = LocalDate.of(2023, 2, 2);
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        var emp = Optional.of(Employee.builder().id(id)
                .humanReadableId("TEST")
                .departmentId(id)
                .organizationId(id).build());
        List<EmpLimit> empLimitList = new ArrayList<>();
        doReturn(empLimitList)
                .when(empLimitRepository)
                .findByEmployeeIdAndYearAndLimitStatusAndLimitType(emp.get().getId()
                        , 2023, LimitStatus.SHARED, LimitType.EMPLOYEE);
        DepLimit depLimit = new DepLimit();
        var depart = Department.builder().id(id).parentId(parentId).build();
        depLimit.setDepartment(depart);
        depLimit.setYear(2023);
        depLimit.setLimitType(LimitType.DEPARTMENT);
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);

        var limitSharingDep = LimitSharing.builder().limit(depLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriodDep = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(100))
                .balance(BigDecimal.valueOf(9))
                .limitSharing(limitSharingDep).build();
        limitSharingDep.getSharingPerPeriods().add(limitSharingByPeriodDep);
        List<DepLimit> depLimitListEmpty = new ArrayList<>();
        depLimit.setUseThisLimit(true);
        depLimit.getSharings().add(limitSharingDep);
        doReturn(depLimitListEmpty)
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitStatusAndLimitType(emp.get().getDepartmentId()
                        , 2023, LimitStatus.SHARED, LimitType.DEPARTMENT);
        doReturn(Optional.empty())
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNot(id, 2023, "CARGO", LimitStatus.CLOSED);
        doReturn(Optional.of(depLimit))
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNot(eq(parentId), anyInt(), eq("CARGO"),
                        eq(LimitStatus.CLOSED));
        doReturn(Optional.of(depart))
                .when(departmentRepository)
                .findById(id);
        var result = service.getLimiInfoByDate(emp.get(), date, TransportTypeEnum.DEDICATED);
        assertEquals(BigDecimal.valueOf(100), result.getSum());
        assertEquals(BigDecimal.valueOf(9), result.getBalance());
        assertEquals(2023, result.getYear());
        assertEquals(LimitSharingType.MONTHLY, result.getLimitSharingType());
        assertEquals(TransportTypeEnum.DEDICATED, result.getTransportType());
    }

    @Test
    @DisplayName("Нет лимита никакого")
    void getLimiInfoByDate5() {
        var date = LocalDate.of(2023, 2, 2);
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        var emp = Optional.of(Employee.builder().id(id)
                .humanReadableId("TEST")
                .departmentId(id)
                .organizationId(id).build());
        List<EmpLimit> empLimitList = new ArrayList<>();
        doReturn(empLimitList)
                .when(empLimitRepository)
                .findByEmployeeIdAndYearAndLimitStatusAndLimitType(emp.get().getId()
                        , 2023, LimitStatus.SHARED, LimitType.EMPLOYEE);
        DepLimit depLimit = new DepLimit();
        var depart = Department.builder().id(id).parentId(parentId).build();
        depLimit.setDepartment(depart);
        depLimit.setYear(2023);
        depLimit.setLimitType(LimitType.DEPARTMENT);
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);

        var limitSharingDep = LimitSharing.builder().limit(depLimit).sum(BigDecimal.valueOf(444))
                .balance(BigDecimal.valueOf(333))
                .transportType(TransportTypeEnum.DEDICATED)
                .build();
        var limitSharingByPeriodDep = LimitSharingPerPeriod.builder()
                .periodData(PeriodData.FEBRUARY)
                .sum(BigDecimal.valueOf(100))
                .balance(BigDecimal.valueOf(9))
                .limitSharing(limitSharingDep).build();
        limitSharingDep.getSharingPerPeriods().add(limitSharingByPeriodDep);
        List<DepLimit> depLimitListEmpty = new ArrayList<>();
        depLimit.setUseThisLimit(false);
        depLimit.getSharings().add(limitSharingDep);
        doReturn(depLimitListEmpty)
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitStatusAndLimitType(emp.get().getDepartmentId()
                        , 2023, LimitStatus.SHARED, LimitType.DEPARTMENT);
        doReturn(Optional.empty())
                .when(depLimitRepository)
                .findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNot(id, 2023, "CARGO", LimitStatus.CLOSED);
        var result = service.getLimiInfoByDate(emp.get(), date, TransportTypeEnum.DEDICATED);
        assertEquals(BigDecimal.valueOf(0), result.getSum());
        assertEquals(BigDecimal.valueOf(0), result.getBalance());
        assertEquals(2023, result.getYear());
        assertNull(result.getLimitSharingType());
        assertEquals(TransportTypeEnum.DEDICATED, result.getTransportType());
    }

    @Test
    @DisplayName("Получение лимита сотрудника")
    void test_getLimit_employee() {
        var limit = Instancio.create(EmpLimit.class);

        when(limitRepository.findById(limit.getId())).thenReturn(Optional.of(limit));

        var actual = service.get(limit.getId());

        assertThat(actual).isPresent().hasValue(limit).hasValueSatisfying(act -> assertThat(act).isInstanceOf(EmpLimit.class));
    }

    @Test
    @DisplayName("Получение лимита подразделения без головы")
    void test_getLimit_department_with_no_head() {
        var department = Instancio.of(Department.class)
                .ignore(Select.field(Department::getDepartmentHead))
                .create();

        var limit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getDepartment), department)
                .create();

        when(limitRepository.findById(limit.getId())).thenReturn(Optional.of(limit));

        var actual = service.get(limit.getId());

        assertThat(actual).isPresent().hasValue(limit)
                .hasValueSatisfying(act -> {
                    assertThat(act).isInstanceOf(DepLimit.class);
                    assertThat(((DepLimit) act).getDepartment()).isEqualTo(department);
                    assertThat(((DepLimit) act).getDepartment().getDepartmentHead()).isNull();
                });
    }

    @Test
    @DisplayName("Получение лимита подразделения с головой")
    void test_getLimit_department_with_head() {
        var department = Instancio.create(Department.class);

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), department.getDepartmentHead().getId())
                .create();

        var limit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getDepartment), department)
                .create();

        when(limitRepository.findById(limit.getId())).thenReturn(Optional.of(limit));
        when(employeeRepository.findById(department.getDepartmentHead().getId())).thenReturn(Optional.of(employee));

        var actual = service.get(limit.getId());

        assertThat(actual).isPresent().hasValue(limit)
                .hasValueSatisfying(act -> {
                    assertThat(act).isInstanceOf(DepLimit.class);
                    assertThat(((DepLimit) act).getDepartment()).isEqualTo(department);
                    assertThat(((DepLimit) act).getDepartment().getDepartmentHead()).isNotNull().isEqualTo(employee);
                });
    }

    @DisplayName("Получение детей лимита с указанием типа")
    @Test
    void test_getLimitChildren_limitType() {
        var limit = Instancio.create(DepLimit.class);
        var limitType = Instancio.create(LimitType.class);
        var children = Instancio.ofSet(Limit.class).create();

        when(limitRepository.findByParentAndLimitType(limit, limitType)).thenReturn(children);

        var actualSet = service.getLimitChildren(limit, limitType);

        assertThat(actualSet).hasSameElementsAs(children);
    }

    @DisplayName("Получение детей лимита без указания типа")
    @Test
    void test_getLimitChildren_no_limitType() {
        var limit = Instancio.create(DepLimit.class);
        var children = Instancio.ofSet(Limit.class).create();

        when(limitRepository.findByParent(limit)).thenReturn(children);

        var actualSet = service.getLimitChildren(limit, null);

        assertThat(actualSet).hasSameElementsAs(children);
    }

    @DisplayName("Получение вышестоящих лимитов")
    @Test
    void test_getUpperLevelLimitList() {
        var organizationId = UUID.randomUUID();
        var serviceType = Instancio.create(String.class);
        var year = Instancio.create(Integer.class);
        var limits = Instancio.ofList(Limit.class).create();

        when(limitRepository.findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNull(organizationId, serviceType, year)).thenReturn(limits);

        var actual = service.getUpperLevelLimitList(organizationId, serviceType, year);

        assertThat(actual).hasSameElementsAs(limits);
    }

    @DisplayName("Проверка трансфера суммы. Неверные данные, нет лимитов")
    @Test
    void test_transferSum_dataInvalid_noLimits() {
        var sum = Instancio.create(BigDecimal.class);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);

        try {
            service.transferSum(new LimitData(null, null), new LimitData(null, null), sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: исходный или целевой лимит не найден!");
        }
        try {
            service.transferSum(new LimitData(null, null), Instancio.create(LimitData.class), sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: исходный или целевой лимит не найден!");
        }
        try {
            service.transferSum(Instancio.create(LimitData.class), new LimitData(null, null), sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: исходный или целевой лимит не найден!");
        }
    }

    @DisplayName("Проверка трансфера суммы. Несоответствие лет")
    @Test
    void test_transferSum_dataInvalid_wrongYears() {
        var sum = Instancio.create(BigDecimal.class);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var leftLimit = new DepLimit();
        leftLimit.setYear(1);
        var rightLimit = new DepLimit();
        rightLimit.setYear(2);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);

        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: годы лимитов не совпадают");
        }
    }

    @DisplayName("Проверка трансфера суммы. Несоответствие типов услуг")
    @Test
    void test_transferSum_dataInvalid_wrongTypes() {
        var sum = Instancio.create(BigDecimal.class);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var leftLimit = new DepLimit();
        leftLimit.setLimitServiceType("CARGO");
        var rightLimit = new DepLimit();
        rightLimit.setLimitServiceType("PASSENGER");
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);

        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: типы услуг не совпадают");
        }
    }

    @DisplayName("Проверка трансфера суммы. Несоответствие деревьев лимитов")
    @Test
    void test_transferSum_dataInvalid_wrongParents() {
        var sum = Instancio.create(BigDecimal.class);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit1 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var parentLimit2 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setLimitServiceType("PASSENGER");
        leftLimit.setParent(parentLimit1);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitServiceType("PASSENGER");
        rightLimit.setParent(parentLimit2);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);
        doReturn(Optional.of(parentLimit1)).when(limitRepository).findById(parentLimit1.getId());
        doReturn(Optional.of(parentLimit2)).when(limitRepository).findById(parentLimit2.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: исходный и целевой лимит из разных деревьев");
        }
    }

    @DisplayName("Проверка трансфера суммы. Родительский лимит пуст")
    @Test
    void test_transferSum_parent_null() {
        var sum = BigDecimal.valueOf(10).negate();
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit1 = new DepLimit();
        leftLimit1.setId(UUID.randomUUID());
        leftLimit1.setLimitServiceType("PASSENGER");
        leftLimit1.setParent(null);
        leftLimit1.setLimitStatus(LimitStatus.SHARED);
        var rightLimit1 = new DepLimit();
        rightLimit1.setId(UUID.randomUUID());
        rightLimit1.setLimitStatus(LimitStatus.SHARED);
        rightLimit1.setLimitServiceType("PASSENGER");
        rightLimit1.setParent(parentLimit);
        var leftLimit2 = new DepLimit();
        leftLimit2.setId(UUID.randomUUID());
        leftLimit2.setLimitServiceType("PASSENGER");
        leftLimit2.setParent(parentLimit);
        leftLimit2.setLimitStatus(LimitStatus.SHARED);
        var rightLimit2 = new DepLimit();
        rightLimit2.setId(UUID.randomUUID());
        rightLimit2.setLimitStatus(LimitStatus.SHARED);
        rightLimit2.setLimitServiceType("PASSENGER");
        rightLimit2.setParent(null);
        var left1 = new LimitData(leftLimit1, TransportTypeEnum.TAXI);
        var right1 = new LimitData(rightLimit1, TransportTypeEnum.TAXI);
        var left2 = new LimitData(leftLimit2, TransportTypeEnum.TAXI);
        var right2 = new LimitData(rightLimit2, TransportTypeEnum.TAXI);
        doReturn(Optional.empty()).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left1, right1, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: исходный и целевой лимит из разных деревьев");
        }
        try {
            service.transferSum(left2, right2, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: исходный и целевой лимит из разных деревьев");
        }
    }

    @DisplayName("Проверка трансфера суммы. Исходный лимит закрыт")
    @Test
    void test_transferSum_dataInvalid_sourceClosed() {
        var sum = Instancio.create(BigDecimal.class);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = false;
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setLimitServiceType("PASSENGER");
        leftLimit.setLimitStatus(LimitStatus.CLOSED);
        leftLimit.setParent(parentLimit);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitServiceType("PASSENGER");
        rightLimit.setLimitStatus(LimitStatus.SHARED);
        rightLimit.setParent(parentLimit);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);
        doReturn(Optional.of(parentLimit)).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: Исходный лимит закрыт. Limit id = " + leftLimit.getId());
        }
    }

    @DisplayName("Проверка трансфера суммы. Целевой лимит закрыт")
    @Test
    void test_transferSum_dataInvalid_targetClosed() {
        var sum = Instancio.create(BigDecimal.class);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setLimitStatus(LimitStatus.SHARED);
        leftLimit.setParent(parentLimit);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitStatus(LimitStatus.CLOSED);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        leftLimit.setLimitServiceType("PASSENGER");
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setParent(parentLimit);
        rightLimit.setLimitServiceType("PASSENGER");
        doReturn(Optional.of(parentLimit)).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: Целевой лимит закрыт. Limit id = " + rightLimit.getId());
        }
    }

    @DisplayName("Проверка трансфера суммы. Неверная сумма")
    @Test
    void test_transferSum_dataInvalid_wrongSum() {
        var sum = BigDecimal.valueOf(10).negate();
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setLimitServiceType("PASSENGER");
        leftLimit.setParent(parentLimit);
        leftLimit.setLimitStatus(LimitStatus.SHARED);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitStatus(LimitStatus.SHARED);
        rightLimit.setLimitServiceType("PASSENGER");
        rightLimit.setParent(parentLimit);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);
        doReturn(Optional.of(parentLimit)).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitLogicException.class)
                    .hasMessage("transferSum: Невозможно перевести отрицательную сумму!");
        }
    }

    @DisplayName("Проверка трансфера суммы. Нет исходного шаринга")
    @Test
    void test_transferSum_dataInvalid_noSourceSharing() {
        var sum = BigDecimal.valueOf(10);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setLimitServiceType("PASSENGER");
        leftLimit.setParent(parentLimit);
        leftLimit.setLimitStatus(LimitStatus.SHARED);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitStatus(LimitStatus.SHARED);
        rightLimit.setLimitServiceType("PASSENGER");
        rightLimit.setParent(parentLimit);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);
        doReturn(Optional.of(parentLimit)).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", LimitSharing.class.getSimpleName())
                    .hasFieldOrPropertyWithValue("entityId", Map.of("id", leftLimit.getId().toString(), "transportType", TransportTypeEnum.TAXI))
            ;
        }
    }

    @DisplayName("Проверка трансфера суммы. Нет целевого шаринга")
    @Test
    void test_transferSum_dataInvalid_noTargetSharing() {
        var sum = BigDecimal.valueOf(10);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setLimitServiceType("PASSENGER");
        leftLimit.setParent(parentLimit);
        leftLimit.setLimitStatus(LimitStatus.SHARED);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitStatus(LimitStatus.SHARED);
        rightLimit.setLimitServiceType("PASSENGER");
        rightLimit.setParent(parentLimit);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);

        when(limitSharingService.getByLimitAndTransportType(leftLimit, TransportTypeEnum.TAXI)).thenReturn(Instancio.of(LimitSharing.class).set(Select.field(LimitSharing::getSum), BigDecimal.valueOf(100)).create());
        doReturn(Optional.of(parentLimit)).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", LimitSharing.class.getSimpleName())
                    .hasFieldOrPropertyWithValue("entityId", Map.of("id", rightLimit.getId(), "transportType", TransportTypeEnum.TAXI))
            ;
        }
    }

    @DisplayName("Проверка трансфера суммы. Не хватает денег в исходном лимите")
    @Test
    void test_transferSum_dataInvalid_noSufficientSum() {
        var sum = BigDecimal.valueOf(10);
        var authorId = Instancio.create(UUID.class);
        var limitTransferHistoryType = Instancio.create(LimitTransferHistoryType.class);
        var allowTakingFromClosedSource = Instancio.create(Boolean.class);
        var parentLimit = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), null)
                .create();
        var leftLimit = new DepLimit();
        leftLimit.setId(UUID.randomUUID());
        leftLimit.setHumanReadableId("HRI");
        leftLimit.setLimitServiceType("PASSENGER");
        leftLimit.setParent(parentLimit);
        leftLimit.setLimitStatus(LimitStatus.SHARED);
        var rightLimit = new DepLimit();
        rightLimit.setId(UUID.randomUUID());
        rightLimit.setLimitStatus(LimitStatus.SHARED);
        rightLimit.setLimitServiceType("PASSENGER");
        rightLimit.setParent(parentLimit);
        var left = new LimitData(leftLimit, TransportTypeEnum.TAXI);
        var right = new LimitData(rightLimit, TransportTypeEnum.TAXI);

        when(limitSharingService.getByLimitAndTransportType(leftLimit, TransportTypeEnum.TAXI)).thenReturn(Instancio.of(LimitSharing.class)
                .set(Select.field(LimitSharing::getBalance), BigDecimal.valueOf(5)).set(Select.field(LimitSharing::getLimit), leftLimit).create());
        doReturn(Optional.of(parentLimit)).when(limitRepository).findById(parentLimit.getId());
        try {
            service.transferSum(left, right, sum, authorId, limitTransferHistoryType, allowTakingFromClosedSource);
            fail("Must be an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(LimitNotSufficientException.class)
            ;
        }
    }

    @Test
    @DisplayName("Проверка закрытия лимитов")
    void test_limitClosing() {
        var authorId = Instancio.create(UUID.class);
        var limit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getLimitStatus), LimitStatus.SHARED)
                .supply(Select.field(DepLimit::getChildren), () -> Instancio.ofList(DepLimit.class).set(Select.field(DepLimit::getLimitStatus), LimitStatus.CLOSED).create())
                .supply(Select.field(DepLimit::getAuthor), () -> Instancio.of(Employee.class).set(Select.field(Employee::getId), authorId).create())
                .create();

        var sharings = Instancio.ofList(LimitSharing.class).create();

        when(limitSharingService.getByLimit(limit.getParent())).thenReturn(sharings);

        var result = service.closeLimit(limit, authorId);

        assertThat(result).isTrue();

        var limitDataCaptor = ArgumentCaptor.forClass(LimitData.class);

        verify(limitHistoryService).add(eq(authorId), limitDataCaptor.capture(), eq(null), eq(limit.getSum()), eq(limit.getYear()), eq(LimitHistoryType.CLOSE), eq(null), eq(null));

        assertThat(limitDataCaptor.getValue())
                .isNotNull()
                .satisfies(data -> {
                    assertThat(data.limit()).isEqualTo(limit);
                    assertThat(data.transportType()).isNull();
                    assertThat(data.period()).isNull();
                });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    @DisplayName("Получение верхнеуровневых активных лимитов")
    void test_getUpperLevelActiveLimit() {
        var organizationId = UUID.randomUUID();
        var serviceType = Instancio.create(String.class);
        var year = Instancio.create(Integer.class);
        var limits = Instancio.createList(DepLimit.class).parallelStream().map(Limit.class::cast).toList();

        when(limitRepository.findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNullAndLimitStatusNot(organizationId, serviceType, year, LimitStatus.CLOSED))
                .thenReturn(limits);

        var actual = service.getUpperLevelActiveLimit(organizationId, serviceType, year);
        assertThat(limits).contains(actual.get());
    }

    @Test
    @DisplayName("Получение подразделений")
    void test_getDepartments() {
        var limits = Instancio.createSet(UUID.class);
        var depLimits = Instancio.createList(DepLimit.class);

        when(depLimitRepository.findAll(any(Specification.class))).thenReturn(depLimits);

        var actualMap = service.getDepartments(limits);

        assertThat(actualMap.keySet()).hasSameSizeAs(depLimits).hasSameElementsAs(depLimits.stream().map(DepLimit::getId).toList());
        assertThat(actualMap.values()).hasSameSizeAs(depLimits).hasSameElementsAs(depLimits.stream().map(DepLimit::getDepartment).toList());
        assertThat(actualMap).hasSameSizeAs(depLimits).containsAllEntriesOf(depLimits.stream().collect(Collectors.toMap(DepLimit::getId, DepLimit::getDepartment)));
    }
}