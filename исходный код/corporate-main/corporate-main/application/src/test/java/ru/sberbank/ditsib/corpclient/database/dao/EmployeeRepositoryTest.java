package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория  сотрудников")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class EmployeeRepositoryTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private EmployeeRepository repository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @BeforeEach
    public void saveNeededEntities() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);

        testEmployee1 = repository.save(testEmployee1);
        testEmployee2 = repository.save(testEmployee2);
        testEmployee1.setNew(false);
        testEmployee2.setNew(false);
    }
    
    @Test
    @DisplayName("Тест сохранения сотрудника")
    void test_saveEmployee() {
        repository.save(testEmployee1);
        repository.save(testEmployee2);
        
        assertNotNull(testEmployee2.getId());
        Employee saved = repository.findById(testEmployee2.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Could not find employee"));
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(EMPLOYEE_NAME2, saved.getFirstName());
        assertEquals(EMPLOYEE_LASTNAME2, saved.getLastName());
        assertEquals(EMPLOYEE_PATRONYMIC2, saved.getPatronymic());
        assertEquals(EMPLOYEE_EMAIL2, saved.getEmail());
        assertEquals(testDepartment2, saved.getDepartment());
        assertEquals(testPosition2, saved.getPosition());
        var transportType = saved.getAvailableTransportTypes().iterator().next();
        assertEquals(TransportTypeEnum.PERSONAL, transportType);
        assertEquals(EMPLOYEE_PHONE2, saved.getMobilePhone());
        assertEquals(PERSONNEL_NUMBER_2, saved.getPersonnelNumber());
        assertEquals(testEmployee1, saved.getSupervisor());
        assertNotNull(saved.getSupervisor().getId());
    }
    
    @Test
    @DisplayName("Тест валидации модели сотрудника. Имя")
    void test_EmployeeValidationFirstNameAbsent_throws() {
        testEmployee1.setFirstName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testEmployee1));
    }
    
    @Test
    @DisplayName("Тест валидации модели сотрудника. Фамилия")
    void test_EmployeeValidationLastNameAbsent_throws() {
        testEmployee1.setLastName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testEmployee1));
    }
    
    @Test
    @DisplayName("Тест валидации модели сотрудника. Должность")
    void test_EmployeeValidationPositionAbsent_throws() {
        testEmployee1.setPosition(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testEmployee1));
    }
    
    @Test
    @DisplayName("Тест валидации модели сотрудника. Подразделение")
    void test_EmployeeValidationDepartmentAbsent_throws() {
        testEmployee1.setDepartment(null);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(testEmployee1));
    }
    
    
    @Test
    @DisplayName("Тест удаления сотрудника")
    void test_deleteEmployee() {
        repository.save(testEmployee2);
        
        assertNotNull(testEmployee2.getId());
        Employee saved = repository.findById(testEmployee2.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Could not find " +
                "employee"));
        repository.delete(saved);
        
        Optional<Employee> deleted = repository.findById(testEmployee2.getId());
        assertTrue(deleted.isEmpty());
    }
    
    @Test
    @DisplayName("Тест обновления сотрудника")
    void test_updateEmployee() {
        String newName = "NEW NAME";
        
        repository.save(testEmployee2);
        assertNotNull(testEmployee2.getId());
        Employee saved = repository.findById(testEmployee2.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Could not find " +
                "employee"));
        saved.setFirstName(newName);
        repository.save(saved);
        
        Employee updated = repository.findById(testEmployee2.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Could not find " +
                "employee"));
        assertEquals(newName, updated.getFirstName());
    }
    
    @Test
    @DisplayName("Тест поиска сотрудника")
    void test_searchByFIO() {
        repository.save(testEmployee1);
        repository.save(testEmployee2);
        var searchResult = repository.findByFIOLike(testEmployee1.getFirstName(), testOrganization1.getId(), Pageable.unpaged());
        assertEquals(searchResult.iterator().next(), testEmployee1);
    }
    
}
