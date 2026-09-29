package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.ApproveLimitRequestMessage;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;

/**
 * Маппер, работающий с заявками по лимитам - согласованием.
 */
@Mapper(uses = { PeriodMapper.class, LimitRequestMapper.class, SumMapper.class })
public interface ApproveLimitRequestMapper {

    /**
     * Convert entity to message.
     *
     * @param approver entity.
     * @return message.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentHeadId", source = "department.departmentHead.id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "organizationDepartmentId", source = "department.organizationId")
    @Mapping(target = "organizationEmployeeId", source = "employee.organizationId")
    @Mapping(target = "limitRequestId", source = "limitRequest.id")
    @Mapping(target = "humanReadableId", source = "limitRequest.humanReadableId")
    @Mapping(target = "authorId", source = "limitRequest.author.id")
    @Mapping(target = "creationTime", source = "limitRequest.creationTime")
    @Mapping(target = "transportType", source = "limitRequest.transportType")
    @Mapping(target = "year", source = "limitRequest.year")
    @Mapping(target = "period", source = "limitRequest.periodData")
    @Mapping(target = "status", source = "limitRequest.status")
    @Mapping(target = "limitType", source = "limitRequest.limitType")
    @Mapping(target = "sumLimit", source = "limitRequest.sum")
    @Mapping(target = "description", source = "limitRequest.description")
    @Mapping(target = "declineReason", source = "limitRequest.declineReason")
    @Mapping(target = "sum", source = "sum")
    @Mapping(target = "approvalState", source = "approvalState")
    @Mapping(target = "approvalDate", source = "approvalDate")
    @Mapping(target = "deleted", constant = "false")
    ApproveLimitRequestMessage toMessage(Approver approver);

    /**
     * Convert entity to message.
     *
     * @param approver entity.
     * @return message.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentHeadId", source = "department.departmentHead.id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "organizationDepartmentId", source = "department.organizationId")
    @Mapping(target = "organizationEmployeeId", source = "employee.organizationId")
    @Mapping(target = "limitRequestId", source = "limitRequest.id")
    @Mapping(target = "humanReadableId", source = "limitRequest.humanReadableId")
    @Mapping(target = "authorId", source = "limitRequest.author.id")
    @Mapping(target = "creationTime", source = "limitRequest.creationTime")
    @Mapping(target = "transportType", source = "limitRequest.transportType")
    @Mapping(target = "year", source = "limitRequest.year")
    @Mapping(target = "period", source = "limitRequest.periodData")
    @Mapping(target = "status", source = "limitRequest.status")
    @Mapping(target = "limitType", source = "limitRequest.limitType")
    @Mapping(target = "sumLimit", source = "limitRequest.sum")
    @Mapping(target = "description", source = "limitRequest.description")
    @Mapping(target = "declineReason", source = "limitRequest.declineReason")
    @Mapping(target = "sum", source = "sum")
    @Mapping(target = "approvalState", source = "approvalState")
    @Mapping(target = "approvalDate", source = "approvalDate")
    @Mapping(target = "deleted", constant =  "true")
    ApproveLimitRequestMessage toDeletedMessage(Approver approver);
}
