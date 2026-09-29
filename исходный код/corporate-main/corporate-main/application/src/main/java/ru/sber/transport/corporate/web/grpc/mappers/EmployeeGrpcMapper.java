package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

import static ru.sber.transport.corporate.web.grpc.mappers.ActiveStatusGrpcMapper.DELETED;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper(uses = {ActiveStatusGrpcMapper.class, NullableMapper.class, GenderGrpcMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EmployeeGrpcMapper implements GrpcMapper<Employee, OrganizationsOuterClass.Employee> {

    private ContactsGrpcMapper contactsGrpcMapper;

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @Mapping(target = "itinerantType", source = "itinerant", defaultValue = "NONE")
    @Mapping(target = "employeeType", source = "type")
    @Mapping(target = "deleted", source = "status", qualifiedByName = DELETED)
    @Mapping(target = "contactsList", ignore = true)
    public abstract OrganizationsOuterClass.Employee mapToGrpc(Employee source);

    public OrganizationsOuterClass.Employee toGrpc(Employee source) {
        var builder = mapToGrpc(source).toBuilder();

        builder.addAllContacts(contactsGrpcMapper.toGrpc(source));

        return builder.build();
    }

    @Autowired
    public void setContactsGrpcMapper(ContactsGrpcMapper contactsGrpcMapper) {
        this.contactsGrpcMapper = contactsGrpcMapper;
    }

}
