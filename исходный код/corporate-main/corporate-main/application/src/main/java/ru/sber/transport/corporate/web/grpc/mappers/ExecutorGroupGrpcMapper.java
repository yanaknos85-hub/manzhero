package ru.sber.transport.corporate.web.grpc.mappers;

import com.google.protobuf.Timestamp;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.ObjectUtils;
import ru.sber.transport.corporate.grpc.service.ExecutorGroup;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = org.mapstruct.NullValueCheckStrategy.ALWAYS)
public interface ExecutorGroupGrpcMapper {

    @Mapping(target = "executorsList", source = "executors")
    @Mapping(target = "organizationsList", source = "organizations")
    @Mapping(target = "departmentsList", source = "departments")
    @Mapping(target = "customersList", source = "customers")
    @Mapping(target = "geoZonesList", source = "geoZones")
    @Mapping(target = "contractorsList", source = "contractors")
    ExecutorGroup.GetExecutorGroupByEmployeeIdResponse toGrpc(ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup entity);

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "getFIO")
    @Mapping(target = "employeePersonnelNumber", source = "personnelNumber")
    @Mapping(target = "employeeStatus", source = "activeStatus")
    @Mapping(target = "departmentId", source = "employee.department.id")
    @Mapping(target = "departmentHumanReadableId", source = "employee.department.humanReadableId")
    @Mapping(target = "departmentName", source = "employee.department.name")
    @Mapping(target = "departmentStatus", source = "employee.department.activeStatus")
    ExecutorGroup.Executor executorGrpcFromEmployee(Employee employee);

    @Mapping(target = "name", source = "employee", qualifiedByName = "getFIO")
    @Mapping(target = "status", source = "activeStatus")
    ExecutorGroup.EmployeeExecutorGroup employeeToEmployeeExecutorGroup(Employee employee);

    ExecutorGroup.OrganizationExecutorGroup organizationToExecutorGroup(Organization organization);

    @Mapping(target = "status", source = "activeStatus")
    @Mapping(target = "departmentName", source = "name")
    @Mapping(target = "headNameFirst", ignore = true)
    @Mapping(target = "headNameLast", ignore = true)
    @Mapping(target = "organizationName", ignore = true)
    @Mapping(target = "parentDepartmentName", source = "parent.name")
    @Mapping(target = "easupId", source = "syncId")
    ExecutorGroup.DepartmentExecutorGroup departmentToDepartment(Department department);

    default Timestamp map(LocalDateTime value) {
        return Timestamp.newBuilder()
                .setSeconds(value.toEpochSecond(ZoneOffset.UTC))
                .setNanos(value.getNano())
                .build();
    }

    @Named("getFIO")
    default String getFIO(Employee employee) {
        return employee.getLastName() + " "
                + employee.getFirstName()
                + (!ObjectUtils.isEmpty(employee.getPatronymic()) ? " " + employee.getPatronymic() : "");
    }
}
