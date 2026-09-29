package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.*;
import org.springframework.util.ObjectUtils;
import ru.sber.transport.corporate.messaging.senders.mappers.ContactMessageMapper;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Gender;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.CustomerDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeSelectDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorDTO;
import ru.sberbank.ditsib.corpclient.dto.NewEmployeeDTO;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * Маппер сотрудников.
 */
@Mapper(uses = { DepartmentMapper.class, PositionMapper.class, PersonalCarMapper.class, AttributeMapper.class, DateMapper.class, ActiveStatusMapper.class, ContactMessageMapper.class })
public interface EmployeeMapper {

    @Mapping(target = "departmentId", source = "employee.department.id")
    @Mapping(target = "positionId", source = "employee.position.id")
    @Mapping(target = "positionName", source = "employee.position.name")
    @Mapping(target = "supervisorId", source = "employee.supervisor.id")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "humanReadableId", source = "employee.humanReadableId")
    @Mapping(target = "organizationName", source = "employee.department.organization.officialName")
    @Mapping(target = "departmentName", source = "employee.department.name")
    @Mapping(target = "status", source = "employee.activeStatus")
    @Mapping(target = "roles", ignore = true)
    EmployeeDTO toDto(Employee employee, UUID organizationId);
    
    @Mapping(source = "position.id", target = "positionId")
    @Mapping(source = "supervisor.id", target = "supervisorId")
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(target = "roles", ignore = true)
    EmployeeSelectDTO employeeToSelectDTO(Employee employee);
    
    @Mapping(target = "activeStatus", source = "status")
    @Mapping(target = "position.id", source = "positionId")
    Employee newDTOToEmployee(NewEmployeeDTO dto);
    
    @Mapping(target = "id", source = "employee.id")
    @Mapping(target = "positionId", source = "employee.position.id")
    @Mapping(target = "supervisorId", source = "employee.supervisor.id")
    @Mapping(target = "departmentId", source = "employee.department.id")
    @Mapping(target = "marriageCertificateNumber", source = "employee.marriageCertificateId")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "availableTransportTypes", ignore = true)
    @Mapping(target = "deleted", source = "employee.activeStatus", qualifiedByName = "deleted")
    @Mapping(target = "attributes", source = "attributes")
    EmployeeMessage toMessage(Employee employee, Organization organization, Set<String> attributes);

    @Mapping(target = "positionId", source = "position.id")
    @Mapping(target = "supervisorId", source = "supervisor.id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "deleted", source = "activeStatus", qualifiedByName = "deleted")
    @Mapping(target = "employeeType", source = "orgStructureType")
    @Mapping(target = "itinerantType", source = "itinerantType", defaultValue = "NONE")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "contacts", source = "employee")
    ru.sber.transport.messages.corporate.avro.EmployeeMessage toMessageAvro(Employee employee);

    @Mapping(target = "id", source = "uuid")
    Employee employeeFromId(UUID uuid);

    @Mapping(target = "activeStatus", source = "status")
    @Mapping(target = "orgStructureType", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "phoneConfirmed", ignore = true)
    void toModel(@MappingTarget Employee employee, NewEmployeeDTO newData);

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "getFIO")
    @Mapping(target = "employeePersonnelNumber", source = "personnelNumber")
    @Mapping(target = "employeeStatus", source = "activeStatus")
    @Mapping(target = "departmentId", source = "employee.department.id")
    @Mapping(target = "departmentHumanReadableId", source = "employee.department.humanReadableId")
    @Mapping(target = "departmentName", source = "employee.department.name")
    @Mapping(target = "departmentStatus", source = "employee.department.activeStatus")
    ExecutorDTO employeeFromExecutorDTO(Employee employee);

    @Mapping(target = "name", source = "employee", qualifiedByName = "getFIO")
    @Mapping(target = "status", source = "activeStatus")
    EmployeeExecutorGroupDTO employeeToExecutorGroutDTO(Employee employee);

    CustomerDTO employeeToCustomerDTO(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "personnelNumber", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "delegateRecords", ignore = true)
    @Mapping(target = "availableTransportTypes", ignore = true)
    @Mapping(target = "mobilePhone", ignore = true)
    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "slaves", ignore = true)
    @Mapping(target = "personalCars", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "actions", ignore = true)
    @Mapping(target = "delegatedBy", ignore = true)
    @Mapping(target = "supervisorOf", ignore = true)
    @Mapping(target = "activeStatus", source = "active")
    @Mapping(target = "updateTime", source = "updateDate")
    @Mapping(target = "orgStructureType", constant = "INTERNAL")
    @Mapping(target = "creationTime", ignore = true)
    void update(@MappingTarget Employee target, State.Employee source);

    default ItinerantType fromGrpc(State.ItinerantType source) {
        return ItinerantType.valueOf(source.name());
    }

    default Gender toBusiness(State.Gender source) {
        return Gender.valueOf(source.name());
    }

    void update(@MappingTarget Employee target, Employee source);

    @Named("getItinerantType")
    default ItinerantType getItinerantType(String source) {
        return ItinerantType.getByName(source).orElse(null);
    }

    @Named("getFIO")
    default String getFIO(Employee employee) {
        return employee.getLastName() + " "
                + employee.getFirstName()
                + (!ObjectUtils.isEmpty(employee.getPatronymic()) ? " " + employee.getPatronymic() : "");
    }

    default String toBusiness(State.NullableString source) {
        if (!source.hasValue()) {
            return null;
        }
        return source.getValue();
    }

    @BeforeMapping
    default void setPhoneConfirmed(@MappingTarget Employee target, NewEmployeeDTO newData) {
        if (target != null && newData != null && notEqual(target.getMobilePhone(), newData.mobilePhone())) {
            target.setPhoneConfirmed(false);
        }
    }

}
