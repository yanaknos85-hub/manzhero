package ru.sberbank.ditsib.corpclient.service;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.dto.DelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeParameters;
import ru.sberbank.ditsib.corpclient.dto.GetDelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.exceptions.DataConstrainViolationException;
import ru.sberbank.ditsib.corpclient.exceptions.DelegateAlreadyExistsException;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса делегатов")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@ActiveProfiles("test")
class DelegateRecordServiceTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private DelegateService delegateService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @MockitoBean(name = "delegateOutput")
    private OutputBridge delegateOutput;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void init() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");

        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(testOrganization2);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testDepartment3.setOrganization(testOrganization2);
        testDepartment3 = departmentRepository.save(testDepartment3);

        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment1.getOrganization());

        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.save(testEmployee2);

        testDepartment1.setHead(testEmployee1);
        departmentRepository.save(testDepartment1);

        testDepartment2.setHead(testEmployee2);
        departmentRepository.save(testDepartment2);

    }

    @DisplayName("Добавление")
    @Test
    void test_addDelegate_success() {
        var fromDate = LocalDate.now();
        var toDate = LocalDate.now().plusMonths(1);
        var newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();

        delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(), newDelegateRecordDTO);
        List<GetDelegateRecordDTO> allByDelegate = delegateService.getAllByDelegate(
                testOrganization1.getId(),
                testDepartment1.getId(), testEmployee2.getId());
        assertEquals(1, allByDelegate.size());
        var getDelegateRecordDTO = allByDelegate.getFirst();
        assertEquals(testEmployee1.getId(), getDelegateRecordDTO.getSupervisorId());
        assertEquals(testEmployee2.getId(), getDelegateRecordDTO.getDelegateId());
        assertEquals(fromDate, getDelegateRecordDTO.getStartDate());
        assertEquals(toDate, getDelegateRecordDTO.getEndDate());
        assertEquals(TransportTypeEnum.TAXI, getDelegateRecordDTO.getTransportType());

        //Проверка, что запись сохранена со статусом status

        var id = getDelegateRecordDTO.getId();
        var rowCnt =
                (Long) em
                        .createNativeQuery("SELECT count(*) FROM corporate.delegate a WHERE a.status='ACTIVE' AND" +
                                " id='" + id + "'")
                        .getSingleResult();
        assertEquals(1, rowCnt.intValue(), "Ожидалось, что в БД запись будет сохранена со статусом ACTIVE");

        var message = getMessages(delegateOutput, DelegateMessage.class);
        assertThat(message.getDelegateId()).isEqualTo(getDelegateRecordDTO.getDelegateEmployee().id());
        assertThat(message.getEndDate()).isEqualTo(getDelegateRecordDTO.getEndDate());
        assertThat(message.getStartDate()).isEqualTo(getDelegateRecordDTO.getStartDate());
        assertThat(message.getId()).isEqualTo(getDelegateRecordDTO.getId());
        assertThat(message.getSupervisorId()).isEqualTo(getDelegateRecordDTO.getSupervisorId());
        assertThat(message.getTransportTypeId()).isEqualTo(getDelegateRecordDTO.getTransportType().getId());
        assertThat(message.isDeleted()).isFalse();
    }

    @DisplayName("Добавление повторяющихся данных")
    @Test
    void test_addDelegateDuplicate_throws() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusMonths(1);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(), newDelegateRecordDTO);
        try {
            delegateService.add(
                    testEmployee1.getDepartment().getOrganization().getId(),
                    testEmployee1.getDepartment().getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DelegateAlreadyExistsException.class);
        }
    }

    @DisplayName("Добавление с пересекающимися диапазонами дат")
    @Test
    void test_addDelegateCrossingRanges_throws() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusMonths(2);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(), newDelegateRecordDTO);
        newDelegateRecordDTO.setStartDate(newDelegateRecordDTO.getStartDate().plusMonths(1));
        try {
            delegateService.add(testOrganization1.getId(),
                    testDepartment1.getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DelegateAlreadyExistsException.class);
        }
    }
    @DisplayName("Добавление (проверка даты раньше текущей)")
    @Test
    void test_addPastDate_throws() {
        LocalDate fromDate = LocalDate.now().minusMonths(1);
        LocalDate toDate = LocalDate.now().plusMonths(1);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();

        try {
            delegateService.add(
                    testEmployee1.getDepartment().getOrganization().getId(),
                    testEmployee1.getDepartment().getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DataConstrainViolationException.class);
        }

        newDelegateRecordDTO.setStartDate(fromDate.plusMonths(2));
        newDelegateRecordDTO.setEndDate(toDate.minusMonths(2));
        try {
            delegateService.add(
                    testEmployee1.getDepartment().getOrganization().getId(),
                    testEmployee1.getDepartment().getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DataConstrainViolationException.class);
        }
    }

    @DisplayName("Редактирование ")
    @Test
    void test_edit() {
        var fromDate = LocalDate.now();
        var toDate = LocalDate.now().plusMonths(1);
        var newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        var persisted1 = delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(),
                newDelegateRecordDTO);
        getMessages(delegateOutput, DelegateMessage.class);

        newDelegateRecordDTO.setStartDate(fromDate.plusMonths(2));
        newDelegateRecordDTO.setEndDate(toDate.plusMonths(2));

        var updated = delegateService.edit(
                testEmployee1.getDepartment().getOrganization().getId(),
                testEmployee1.getDepartment().getId(),
                persisted1.getId(),
                newDelegateRecordDTO);
        assertEquals(persisted1.getId(), updated.getId());
        assertEquals(newDelegateRecordDTO.getStartDate(), updated.getStartDate());
        assertEquals(newDelegateRecordDTO.getEndDate(), updated.getEndDate());
        assertEquals(persisted1.getSupervisorId(), updated.getSupervisorId());
        assertEquals(persisted1.getDelegateId(), updated.getDelegateId());
        assertNotNull(updated.getDelegateEmployee());
        assertEquals(persisted1.getDelegateId(), updated.getDelegateEmployee().id());

        var message = getMessages(delegateOutput, 2, DelegateMessage.class).get(1);
        assertThat(message.getDelegateId()).isEqualTo(persisted1.getDelegateId());
        assertThat(message.getEndDate()).isEqualTo(newDelegateRecordDTO.getEndDate());
        assertThat(message.getStartDate()).isEqualTo(newDelegateRecordDTO.getStartDate());
        assertThat(message.getId()).isEqualTo(persisted1.getId());
        assertThat(message.getSupervisorId()).isEqualTo(persisted1.getSupervisorId());
        assertThat(message.getTransportTypeId()).isEqualTo(updated.getTransportType().getId());
        assertThat(message.isDeleted()).isFalse();
    }

    @DisplayName("Редактирование (проверка на повторяющиеся данные)")
    @Test
    void test_editDelegateDuplicate_throws() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusMonths(1);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        GetDelegateRecordDTO persested1 = delegateService.add(testEmployee1.getDepartment().getOrganization().getId(),
                testEmployee1.getDepartment().getId(),
                newDelegateRecordDTO);


        newDelegateRecordDTO.setStartDate(fromDate.plusMonths(2));
        newDelegateRecordDTO.setEndDate(toDate.plusMonths(2));

        delegateService.add(testEmployee1.getDepartment().getOrganization().getId(),
                testEmployee1.getDepartment().getId(),
                newDelegateRecordDTO);

        System.err.printf("%s : %s : %s : %s : %s%n", persested1.getId(), newDelegateRecordDTO.getSupervisorId(),
                newDelegateRecordDTO.getDelegateId(), newDelegateRecordDTO.getTransportType(), newDelegateRecordDTO.getStartDate());

        try {
            delegateService.edit(
                    testOrganization1.getId(),
                    testDepartment1.getId(),
                    persested1.getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DelegateAlreadyExistsException.class);
        }
    }

    @DisplayName("Редактирование (проверка даты раньше текущей)")
    @Test
    void test_editUpdateDate_throws() {
        var fromDate = LocalDate.now();
        var toDate = LocalDate.now().plusMonths(1);
        var newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        var persisted1 = delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(),
                newDelegateRecordDTO);
        getMessages(delegateOutput, DelegateMessage.class);

        newDelegateRecordDTO.setStartDate(fromDate.minusMonths(1));
        try {
            delegateService.edit(
                    testEmployee1.getDepartment().getOrganization().getId(),
                    testEmployee1.getDepartment().getId(),
                    persisted1.getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DataConstrainViolationException.class);
        }

        newDelegateRecordDTO.setStartDate(fromDate.plusMonths(2));
        newDelegateRecordDTO.setEndDate(toDate.minusMonths(2));
        try {
            delegateService.edit(
                    testEmployee1.getDepartment().getOrganization().getId(),
                    testEmployee1.getDepartment().getId(),
                    persisted1.getId(),
                    newDelegateRecordDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DataConstrainViolationException.class);
        }
    }

    @DisplayName("Редактирование с пересекающимися диапазонами дат")
    @Test
    void test_editDelegateCrossingRanges_throws() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusMonths(1);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        GetDelegateRecordDTO persested1 = delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(),
                newDelegateRecordDTO);


        newDelegateRecordDTO.setStartDate(fromDate.plusMonths(2));
        newDelegateRecordDTO.setEndDate(toDate.plusMonths(2));

        delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(),
                newDelegateRecordDTO);

        System.err.println(persested1.getId() + " : " + newDelegateRecordDTO.getSupervisorId() + " : " +
                newDelegateRecordDTO.getDelegateId()
                + " : " + newDelegateRecordDTO.getTransportType() + " : " +
                newDelegateRecordDTO.getStartDate());

        newDelegateRecordDTO.setStartDate(fromDate.plusMonths(1));
        newDelegateRecordDTO.setEndDate(toDate.plusMonths(2));

        try {
            delegateService.edit(testOrganization1.getId(),
                    testDepartment1.getId(),
                    persested1.getId(),
                    newDelegateRecordDTO);
        } catch (DelegateAlreadyExistsException e) {
            assertThat(e.getMessage()).contains("crossing specified date");
        }

    }

    @DisplayName("Получение кандидатов для делегирования (нет кандидатов)")
    @Test
    void test_getAllCandidates_empty() {
        var transportType = TransportTypeEnum.TAXI;
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusMonths(1);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(),
                newDelegateRecordDTO);
        var candidates = delegateService.getAllCandidates(testOrganization1.getId(),
                testDepartment1.getId(),
                testEmployee1.getId(),
                transportType,
                fromDate.plusDays(10),
                new EmployeeParameters());
        assertThat(candidates).isEmpty();
    }

    @DisplayName("Получение кандидатов для делегирования (1 кандидат)")
    @Test
    void test_getAllCandidates_one() {
        var transportType = TransportTypeEnum.TAXI;
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusMonths(1);
        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(fromDate)
                        .endDate(toDate)
                        .transportType(TransportTypeEnum.TAXI).build();
        delegateService.add(testOrganization1.getId(),
                testDepartment1.getId(),
                newDelegateRecordDTO);
        var candidates = delegateService.getAllCandidates(testOrganization1.getId(),
                testDepartment1.getId(),
                testEmployee1.getId(),
                transportType,
                fromDate.plusDays(35),
                new EmployeeParameters());
        assertThat(candidates).hasSize(1);
    }

    @DisplayName("Получение кандидатов для делегирования (Поиск по ФИО)")
    @Test
    void test_getAllCandidates_find_by_fio() {
        var employeeParameters = new EmployeeParameters();
        var transportType = TransportTypeEnum.TAXI;
        var fromDate = LocalDate.now();
        var employees = new ArrayList<Employee>();
        for (int i = 0; i < 10; i++) {
            employees.add(
                    employeeRepository.save(
                            generateEmployee(testDepartment1,
                                    positionRepository.findAll().stream().findAny()
                                            .orElseThrow(() -> new RuntimeException("Position is null")),
                                    testOrganization1)
                    )
            );
        }
        Collections.shuffle(employees);
        var lastName = employees.getFirst().getLastName();
        employeeParameters.setFullName(lastName);
        var candidates = delegateService.getAllCandidates(testOrganization1.getId(),
                testDepartment1.getId(),
                testEmployee1.getId(),
                transportType,
                fromDate.plusDays(10),
                employeeParameters);
        assertThat(candidates).hasSizeGreaterThanOrEqualTo(1);
        assertThat(candidates.iterator().next().lastName()).isEqualTo(lastName);
    }

    @DisplayName("Получение кандидатов для делегирования (Поиск по табельнику)")
    @Test
    void test_getAllCandidates_fing_by_pn() {
        EmployeeParameters employeeParameters = new EmployeeParameters();
        var transportType = TransportTypeEnum.TAXI;
        LocalDate fromDate = LocalDate.now();
        var employees = new ArrayList<Employee>();
        for (int i = 0; i < 10; i++) {
            employees.add(
                    employeeRepository.save(
                            generateEmployee(testDepartment1,
                                    positionRepository.findAll().stream().findAny()
                                            .orElseThrow(() -> new RuntimeException("Position is null")),
                                    testOrganization1)
                    )
            );
        }
        Collections.shuffle(employees);
        var personnelNumber = employees.getFirst().getPersonnelNumber();
        employeeParameters.setPersonnelNumber(personnelNumber);
        var candidates = delegateService.getAllCandidates(testOrganization1.getId(),
                testDepartment1.getId(),
                testEmployee1.getId(),
                transportType,
                fromDate.plusDays(10),
                employeeParameters);
        assertThat(candidates).hasSize(1);
        assertThat(candidates.iterator().next().personnelNumber()).isEqualTo(personnelNumber);
    }

    @DisplayName("Проверка исключения не соответствия дат делегирования")
    @Test
    void test_dataConstrainViolationException() {
        Map<String, Serializable> conflicted = new LinkedHashMap<>();
        conflicted.put("startDate", LocalDate.now().minusMonths(2));
        conflicted.put("endDate", LocalDate.now().minusMonths(1));

        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now().minusMonths(2))
                        .endDate(LocalDate.now().minusMonths(1))
                        .transportType(TransportTypeEnum.TAXI).build();

        var dataConstrainViolationException = new DataConstrainViolationException(
                DelegateRecordDTO.class,
                DataConstrainViolationException.ConflictType.DATE_CONSTRAIN_VIOLATION,
                newDelegateRecordDTO.getDelegateId(),
                conflicted);

        assertEquals("f10bcc5b-51db-4e1c-a747-2a229604f975", dataConstrainViolationException.getChanged().toString());
        assertEquals("DATE_CONSTRAIN_VIOLATION", dataConstrainViolationException.getType().toString());
        assertEquals("DelegateRecordDTO", dataConstrainViolationException.getEntity().getSimpleName());
        assertEquals(LocalDate.now().minusMonths(2), dataConstrainViolationException.getConflicted().get("startDate"));
        assertEquals(LocalDate.now().minusMonths(1), dataConstrainViolationException.getConflicted().get("endDate"));
    }

}
