package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория личных автомобилей")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class PersonalCarRepositoryTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PersonalCarRepository personalCarRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @BeforeEach
    void saveNeededEntities() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.save(testEmployee2);
    }

    
    @Test
    @DisplayName("Тест сохранения личного автомобиля")
    void test_savePersonalAuto() {
        
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setNew(true);
        employee.setDepartment(testDepartment1);
        employee.setFirstName("testname");
        employee.setPosition(testPosition1);
        employee.setLastName("lastname");
        employee.setHumanReadableId("sethuman");
        employee.setPersonnelNumber(Instancio.create(String.class));
        employee.setOrganization(testDepartment1.getOrganization());
        employee.setUpdateTime(OffsetDateTime.now());
        employee = employeeRepository.save(employee);
    
        PersonalCar personalCar1 = new PersonalCar();
        personalCar1.setRegistrationNumber("Test number1");
        personalCar1.setEmployee(employee);
        employee.getPersonalCars().add(personalCar1);
        
        PersonalCar personalCar2 = new PersonalCar();
        personalCar2.setRegistrationNumber("Test number2");
        personalCar2.setEmployee(employee);
        employee.getPersonalCars().add(personalCar2);
        
        personalCar1 = personalCarRepository.save(personalCar1);
        personalCarRepository.save(personalCar2);
        assertEquals(2, personalCarRepository.count());
        
        PersonalCar savePersonalCar1 = personalCarRepository.findById(personalCar1.getId()).orElseThrow(
                () -> new EntityNotFoundException("Could not find entity"));
        
        assertEquals(2, savePersonalCar1.getEmployee().getPersonalCars().size());
    }
    
    @Test
    @DisplayName("Тест сохранения личного автомобиля")
    void test_deletePersonalAuto() {
        testPersonalCar1.setEmployee(testEmployee1);
        testPersonalCar2.setEmployee(testEmployee2);
        personalCarRepository.save(testPersonalCar1);
        personalCarRepository.save(testPersonalCar2);
        assertEquals(2, personalCarRepository.count());
        
        personalCarRepository.deleteById(testPersonalCar1.getId());
        personalCarRepository.deleteById(testPersonalCar2.getId());
        
        assertEquals(0, personalCarRepository.count());
        
        
    }
}
