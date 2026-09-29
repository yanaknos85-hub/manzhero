package ru.sber.transport.utils;

import org.jetbrains.annotations.NotNull;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class TestHelper {
    public static GeoZone createGeoZone(int i) {
        return GeoZone.builder()
                .name("TestZone"+i)
                .id(UUID.randomUUID())
                .code("123456 "+i)
                .parentId(UUID.randomUUID())
                .build();

    }

    private void TestHelper(){}

    @NotNull
    public static Position createPosition(Organization organization) {
        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        return position;
    }

    @NotNull
    public static Employee createEmployee(Department department, int i, Position position) {
        var employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setNew(true);
        employee.setDepartment(department);
        employee.setLastName("Last%sName%02d".formatted(i / 10, i));
        employee.setFirstName("First%sName%02d".formatted(i / 10, i));
        employee.setPersonnelNumber("Personnel%sNumber%02d".formatted(i / 10, i));
        employee.setPatronymic("Patrony%smic%02d".formatted(i / 10, i));
        employee.setHumanReadableId("Human%sReadable%02d".formatted(i / 10, i));
        employee.setMobilePhone("Mob%sile%02d".formatted(i / 10, i));
        employee.setEmail("Em%sail%02d".formatted(i / 10, i));
        employee.setPosition(position);
        employee.setOrganization(department.getOrganization());
        employee.setUpdateTime(OffsetDateTime.now());
        return employee;
    }

    @NotNull
    public static Department createDepartment(Organization organization) {
        return createDepartment(organization, 0);
    }
    @NotNull
    public static Department createDepartment(Organization organization, int i) {
        var department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("Department%s".formatted(i));
        department.setCode("Code%s".formatted(i));
        department.setOrganization(organization);
        department.setHumanReadableId("HRD%s".formatted(i));
        department.setUpdateTime(OffsetDateTime.now());
        department.setLocation("Location %s".formatted(i));
        Employee head = createEmployee(department, i, createPosition(organization));
        head.setFirstName("HeadFirstName%s".formatted(i));
        head.setLastName("HeadLastName%s".formatted(i));
        department.setHead(head);
        return department;
    }

    @NotNull
    public static Organization createOrganization() {
        return createOrganization(0);
    }

    @NotNull
    public static Organization createOrganization(int i) {
        var organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setAddress("address" + i);
        organization.setOfficialName("name" + i );
        organization.setMsrn("msrn" + i);
        organization.setTid("tid" + i);
        return organization;
    }

    @NotNull
    public static ExecutorGroup createExecutorGroup(UUID executorGroupId, LocalDateTime creationTime,
                                                    LocalDateTime updateTime, Set<UUID> contractors) {
        ExecutorGroup executorGroup = new ExecutorGroup();
        executorGroup.setId(executorGroupId);
        executorGroup.setHumanReadableId("ExecutorGroupId");
        executorGroup.setName("ExecutorGroupName");
        executorGroup.setService("ServiceName");
        executorGroup.setActive(true);
        executorGroup.setServiceLevel("ServiceLevelName");
        executorGroup.setCreationTime(creationTime);
        executorGroup.setUpdateTime(updateTime);
        executorGroup.setContractors(contractors);
        return executorGroup;
    }
}
