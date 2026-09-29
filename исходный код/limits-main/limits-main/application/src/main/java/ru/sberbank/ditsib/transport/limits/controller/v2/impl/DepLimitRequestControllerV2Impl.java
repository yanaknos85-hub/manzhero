package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestAskTargets;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.controller.v2.DepLimitRequestController;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.ActionNotAuthorizedException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.LimitRequestMapper;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.DepartmentService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;

@RestController
@RequiredArgsConstructor
@Scope("request")
@Slf4j
class DepLimitRequestControllerV2Impl extends BaseControllerImpl implements DepLimitRequestController {

    private final EmployeeService employeeService;

    private final DepartmentService departmentService;

    private final DepLimitService depLimitService;

    private final LimitRequestService limitRequestService;

    private final LimitRequestMapper limitRequestMapper;

    @Override
    public GetLimitRequestV2DTO addRequest(DepLimitRequestV2DTO newRequestDTO, JwtAuthenticationToken authentication) {
        var author = getEmployee(employeeService, authentication);

        var department = departmentService.get(author.getDepartmentId()).orElse(null);
        if (department == null) {
            throw new EntityNotFoundException(Department.class, author.getDepartmentId());
        }
        var depLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(),
                newRequestDTO.year(),
                newRequestDTO.transportType().getServiceType()
                        .equals(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                        ? "PASSENGER"
                        : "CARGO");
        checkLimit(newRequestDTO, author, depLimit);

        var limitRequest = limitRequestMapper.toModel(newRequestDTO);
        limitRequest.setAuthor(author);
        limitRequest.setOrganizationId(depLimit.getOrganization().getId());
        limitRequest.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));

        List<Approver> approverList = new ArrayList<>();
        if (newRequestDTO.askTargets() == LimitRequestAskTargets.PARENT) {
            var parentLimitDepartment = Optional.ofNullable(depLimit.getParent())
                    .map(Limit::getId)
                    .flatMap(depLimitService::get)
                    .map(DepLimit::getDepartment)
                    .orElse(null);

            UUID limitOwnerId = Optional.ofNullable(parentLimitDepartment)
                    .map(Department::getDepartmentHead)
                    .map(Employee::getId)
                    .orElse(null);

            approverList.add(getApprover(depLimit, limitRequest, parentLimitDepartment, limitOwnerId));
        }
        if (newRequestDTO.askTargets() == LimitRequestAskTargets.SIBLINGS) {
            for (UUID depSiblingId : newRequestDTO.departments()) {
                var dep = departmentService.get(depSiblingId).orElseThrow(() -> new EntityNotFoundException(Department.class, depSiblingId));
                var depLimitSibling =
                        depLimitService.getByDepartmentAndYearAndLimitServiceType(depSiblingId, newRequestDTO.year(),
                                getLimitServiceTypeByTransportType(newRequestDTO.transportType()).name());
                if (depLimitSibling == null) {
                    throw new EntityNotFoundException(Limit.class, depSiblingId);
                }
                approverList.add(getApprover(depLimit, limitRequest, dep, depLimitSibling.getDepartment().getDepartmentHead().getId()));
            }
        }

        limitRequest.setApproverList(approverList);
        limitRequest = limitRequestService.add(limitRequest);

        return limitRequestMapper.toV2Dto(limitRequest);
    }

    @Override
    public void editRequest(UUID requestId, DepLimitRequestV2DTO newData, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var limitRequest = limitRequestService.get(requestId).orElseThrow(
                () -> new EntityNotFoundException(LimitRequest.class, requestId));
        if (!limitRequest.getAuthor().getUserId().equals(userId)) {
            throw new ActionNotAuthorizedException("Logged in user is not the author of the request");
        }
        if (limitRequest.getStatus() != LimitRequestStatus.INIT) {
            throw new LimitLogicException("Request cannot be edited in the status `INIT`");
        }

        limitRequestMapper.update(limitRequest, newData);
        limitRequestService.save(limitRequest);
    }

    private void checkLimit(DepLimitRequestV2DTO newRequestDTO, Employee author, DepLimit depLimit) {
        if (depLimit == null) {
            throw new EntityNotFoundException(Department.class, author.getDepartmentId());
        }
        if (depLimit.getLimitStatus() != LimitStatus.SHARED) {
            throw new LimitLogicException("Limit is not `SHARED`");
        }
        if (!author.getId().equals(depLimit.getDepartment().getDepartmentHead().getId())) {
            throw new ActionNotAuthorizedException("Request for the limit replenishment can by authored only by owner");
        }
        if (depLimit.getParent() == null) {
            throw new LimitLogicException("Limit of the upper-level department cannot be replenishmented");
        }
        if (newRequestDTO.askTargets() == null) {
            throw new LimitLogicException("Source of replenishment is not defined");
        }
    }

    private Approver getApprover(DepLimit depLimit, LimitRequest limitRequest, Department dep, UUID limitOwnerId) {
        if (limitOwnerId == null) {
            throw new LimitLogicException("Limit owner is not found");
        }
        var limitOwner = employeeService.get(limitOwnerId).orElseThrow(() -> new EntityNotFoundException(Employee.class, limitOwnerId));
        var approver = new Approver();
        approver.setDepartment(dep);
        approver.setOrganizationId(depLimit.getOrganization().getId());
        approver.setEmployee(limitOwner);
        approver.setLimitRequest(limitRequest);
        return approver;
    }
}
