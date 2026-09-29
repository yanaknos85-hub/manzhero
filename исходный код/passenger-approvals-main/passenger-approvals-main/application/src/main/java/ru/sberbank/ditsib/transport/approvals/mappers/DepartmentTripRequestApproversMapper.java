package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.approvals.messaging.DepartmentTripRequestApproversMessage;
import ru.sberbank.ditsib.transport.approvals.database.model.Approver;

import java.util.Collection;
import java.util.UUID;

/**
 * Маппер согласующих подразделения.
 */
@Mapper
public interface DepartmentTripRequestApproversMapper {
    @Mapping(target = "departmentId", source = "departmentId")
    @Mapping(target = "approvers", source = "approvers")
    DepartmentTripRequestApproversMessage toMessage(UUID departmentId, Collection<Approver> approvers);
    
    DepartmentTripRequestApproversMessage.Approver toMessage(Approver approver);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Collection<DepartmentTripRequestApproversMessage.Approver> toMessage(Collection<Approver> approver);
    
}
