package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория  делегатов")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@SpringBootTest
class DelegateRepositoryTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private DelegateRepository repository;
    
    
    private DelegateRecord testDelegateRecord1;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @BeforeEach
    public void prepareData() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.saveAndFlush(testDepartment1);
        testDepartment2 = departmentRepository.saveAndFlush(testDepartment2);
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment1);
        testEmployee3.setDepartment(testDepartment2);
        testEmployee4.setDepartment(testDepartment2);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment1.getOrganization());
        testEmployee3.setOrganization(testDepartment2.getOrganization());
        testEmployee4.setOrganization(testDepartment2.getOrganization());
        testEmployee1.setPosition(testPosition1);
        testEmployee2.setPosition(testPosition1);
        testEmployee3.setPosition(testPosition2);
        testEmployee4.setPosition(testPosition2);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        employeeRepository.saveAndFlush(testEmployee4);
        testDepartment2.setHead(testEmployee3);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.saveAndFlush(testEmployee2);
        testDelegateRecord1 =
                DelegateRecord.builder()
                              .supervisor(testEmployee1)
                              .delegate(testEmployee2)
                              .startDate(LocalDate.now())
                              .endDate(LocalDate.now().plusMonths(1))
                              .transportType(TransportTypeEnum.TAXI)
                              .build();
    }
    
    @Test
    @DisplayName("Тест валидации модели  записи о делегате. Отсутствует руководитель")
    void test_delegateValidationSupervisorAbsent_throws() {
        testDelegateRecord1.setSupervisor(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testDelegateRecord1));
    }
    
    @Test
    @DisplayName("Тест валидации модели  записи о делегате. Отсутствует делегат")
    void test_delegateValidationDelegateAbsent_throws() {
        testDelegateRecord1.setDelegate(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testDelegateRecord1));
    }
    
    @Test
    @DisplayName("Тест валидации модели  записи о делегате. Отсутствует дата начала действия")
    void test_delegateValidationStartDateAbsent_throws() {
        testDelegateRecord1.setStartDate(null);
        assertThrows(ConstraintViolationException.class, () -> repository.saveAndFlush(testDelegateRecord1));
    }
    
    @Test
    @DisplayName("Тест валидации модели  записи о делегате. Отсутствует дата окончания действия")
    void test_delegateValidationEndDateAbsent_throws() {
        testDelegateRecord1.setEndDate(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testDelegateRecord1));
    }
    
    @Test
    @DisplayName("Тест сохранения с повторяющимися данными")
    void test_uniqueConstraintViolation_throws() {
        repository.saveAndFlush(testDelegateRecord1);
        
        try {
            repository.saveAndFlush(DelegateRecord.builder().supervisor(testEmployee1)
                                                  .delegate(testEmployee2)
                                                  .startDate(LocalDate.now())
                                                  .endDate(LocalDate.now().plusMonths(1))
                                                  .transportType(TransportTypeEnum.TAXI)
                                                  .build());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
        
    }
}
