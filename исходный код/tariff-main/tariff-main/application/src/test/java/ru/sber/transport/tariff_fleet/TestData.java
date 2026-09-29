package ru.sber.transport.tariff_fleet;

import lombok.experimental.UtilityClass;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Position;

import java.util.UUID;

@UtilityClass
public class TestData {
    
    public static final String OFFICIAL_NAME_1 = "officialName1";
    public static final String OFFICIAL_NAME_2 = "officialName2";
    public static final String POSITION_NAME_1 = "positionName1";
    public static final String POSITION_NAME_2 = "positionName2";
    public static final String DEPARTMENT_NAME_1 = "departmentName1";
    public static final String DEPARTMENT_NAME_2 = "departmentName2";
    public static final UUID ORGANIZATION_1_ID = UUID.randomUUID();
    public static final UUID ORGANIZATION_2_ID = UUID.randomUUID();
    public static final UUID POSITION_1_ID = UUID.randomUUID();
    public static final UUID POSITION_2_ID = UUID.randomUUID();
    public static final UUID DEPARTMENT_1_ID = UUID.randomUUID();
    public static final UUID DEPARTMENT_2_ID = UUID.randomUUID();
    public static final UUID EMPLOYEE_1_ID = UUID.randomUUID();
    public static final UUID EMPLOYEE_3_ID = UUID.randomUUID();
    public static final UUID EMPLOYEE_4_ID = UUID.randomUUID();
    public static final UUID CONTRACTOR_1_ID = UUID.randomUUID();
    public static final UUID CONTRACT_1_ID = UUID.randomUUID();
    public static final String CONTRACT_NUMBER = "0001";
    public static final String DEPARTMENT_HUMAN_READABLE_ID_1 = "DT-0001-00000001";
    public static final String DEPARTMENT_HUMAN_READABLE_ID_2 = "DT-0001-00000002";
    public static final String EMPLOYEE_HUMAN_READABLE_ID_1 = "US-0001-00000001";
    public static final String EMPLOYEE_HUMAN_READABLE_ID_3 = "US-0001-00000003";
    public static final String EMPLOYEE_HUMAN_READABLE_ID_4 = "US-0001-00000004";
    public static final String EMPLOYEE_1_PERSONNEL_NUMBER = "0000001";
    public static final String EMPLOYEE_3_PERSONNEL_NUMBER = "0000003";
    public static final String EMPLOYEE_4_PERSONNEL_NUMBER = "0000004";
    
    public static Organization createOrganization1() {
        return Organization.builder()
                           .id(ORGANIZATION_1_ID)
                           .officialName(OFFICIAL_NAME_1)
                           .digitId(1L)
                           .build();
    }
    
    public static Organization createOrganization2() {
        return Organization.builder()
                           .id(ORGANIZATION_2_ID)
                           .officialName(OFFICIAL_NAME_2)
                           .digitId(2L)
                           .build();
    }
    
    public static Position createPosition1(Organization organization) {
        return Position.builder()
                       .id(POSITION_1_ID)
                       .positionName(POSITION_NAME_1)
                       .organizationId(organization.getId())
                       .build();
    }
    
    public static Position createPosition2(Organization organization) {
        return Position.builder()
                       .id(POSITION_2_ID)
                       .positionName(POSITION_NAME_2)
                       .organizationId(organization.getId())
                       .build();
    }
    
    public static Department createDepartment1(Organization organization, UUID parentId) {
        return Department.builder()
                         .id(DEPARTMENT_1_ID)
                         .humanReadableId(DEPARTMENT_HUMAN_READABLE_ID_1)
                         .organizationId(organization.getId())
                         .departmentName(DEPARTMENT_NAME_1)
                         .parentId(parentId)
                         .easupId("10110907")
                         .build();
    }
    
    public static Department createDepartment2(Organization organization, UUID parentId) {
        return Department.builder()
                         .id(DEPARTMENT_2_ID)
                         .humanReadableId(DEPARTMENT_HUMAN_READABLE_ID_2)
                         .organizationId(organization.getId())
                         .departmentName(DEPARTMENT_NAME_2)
                         .parentId(parentId)
                         .easupId("10110312")
                         .build();
    }
    
    public static Employee createEmployee1(Organization organization, Department department, Position position) {
        return Employee.builder()
                       .id(EMPLOYEE_1_ID)
                       .personnelNumber(EMPLOYEE_1_PERSONNEL_NUMBER)
                       .patronymic("Александровна")
                       .lastName("Гришина")
                       .firstName("Светлана")
                       .mobilePhone("+792356811")
                       .department(department)
                       .userId(EMPLOYEE_1_ID)
                       .humanReadableId(EMPLOYEE_HUMAN_READABLE_ID_1)
                       .position(position)
                       .organization(organization)
                       .costCenter("5200L99780")
                       .build();
    }
    
    public static Employee createEmployee3(Organization organization, Department department, Position position) {
        return Employee.builder()
                       .id(EMPLOYEE_3_ID)
                       .personnelNumber(EMPLOYEE_3_PERSONNEL_NUMBER)
                       .patronymic("Григорьевич")
                       .lastName("Колесниковенко")
                       .firstName("Андрей")
                       .mobilePhone("+792356813")
                       .department(department)
                       .userId(EMPLOYEE_3_ID)
                       .humanReadableId(EMPLOYEE_HUMAN_READABLE_ID_3)
                       .position(position)
                       .organization(organization)
                       .build();
    }
    
    public static Employee createEmployee4(Organization organization, Department department, Position position) {
        return Employee.builder()
                       .id(EMPLOYEE_4_ID)
                       .personnelNumber(EMPLOYEE_4_PERSONNEL_NUMBER)
                       .patronymic("Семеновна")
                       .lastName("Лимаренко")
                       .firstName("Оксана")
                       .mobilePhone("+792356814")
                       .department(department)
                       .userId(EMPLOYEE_4_ID)
                       .humanReadableId(EMPLOYEE_HUMAN_READABLE_ID_4)
                       .position(position)
                       .organization(organization)
                       .build();
    }
}
