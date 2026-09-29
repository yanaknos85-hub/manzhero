package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.controller.v2.EmpLimitRequestV2Controller;
import ru.sberbank.ditsib.transport.limits.dto.LimitRequestApproveDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.EmpLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.ActionNotAuthorizedException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitProblemException;
import ru.sberbank.ditsib.transport.limits.mapper.LimitRequestMapper;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.DepartmentService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;

@Slf4j
@RequiredArgsConstructor
@RestController
@Scope("request")
class EmpLimitRequestV2ControllerImpl extends BaseControllerImpl implements EmpLimitRequestV2Controller {

    private final EmployeeService employeeService;

    private final DepartmentService departmentService;

    private final LimitRequestMapper limitRequestMapper;

    private final LimitRequestService limitRequestService;

    private final DepLimitService depLimitService;

    @Override
    public GetLimitRequestV2DTO addRequest(EmpLimitRequestV2DTO newRequest, JwtAuthenticationToken authentication) {
        var author = getEmployee(employeeService, authentication);

        int currentYear = LocalDate.now(ZoneOffset.UTC).getYear();
        if (newRequest.year() < currentYear) {
            throw new LimitLogicException("Год раньше текущего года");
        }
        var department = departmentService.get(author.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException(Department.class, author.getDepartmentId()));

        var depLimit = getDepartmentLimit(
                department.getId(),
                newRequest.year(),
                getLimitServiceTypeByTransportType(newRequest.transportType()).name());
        if (depLimit == null) {
            throw new LimitProblemException("EmpLimitRequestController: donor limit not found for department " + department.getId());
        }
        if (!depLimit.getDepartment().getId().equals(department.getId()) && !depLimit.isUseThisLimit()) {
            throw new LimitProblemException("EmpLimitRequestController: donor limit with flag 'use my limit' not found for department "
                    + department.getId());
        }
        if (depLimit.getDepartment().getDepartmentHead() == null) {
            throw new LimitLogicException("EmpLimitRequestController: владелец лимита не найден для лимита " + depLimit.getId());
        }
        Employee limitOwner = employeeService.get(depLimit.getDepartment().getDepartmentHead().getId()).orElse(null);

        var limitRequest = limitRequestMapper.toModel(newRequest);
        limitRequest.setAuthor(author);
        limitRequest.setOrganizationId(depLimit.getOrganization().getId());
        limitRequest.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));

        if (author.getId().equals(depLimit.getDepartment().getDepartmentHead().getId())) {
            // автосогласование
            limitRequest.setStatus(LimitRequestStatus.DONE_FULLY);
        }

        var approver = new Approver();
        approver.setDepartment(depLimit.getDepartment());
        approver.setOrganizationId(depLimit.getOrganization().getId());
        approver.setEmployee(limitOwner);
        approver.setLimitRequest(limitRequest);

        List<Approver> approverList = new ArrayList<>();
        approverList.add(approver);
        limitRequest.setApproverList(approverList);
        limitRequest = limitRequestService.add(limitRequest);

        if (LimitRequestStatus.DONE_FULLY.equals(limitRequest.getStatus())) {
            var dto = new LimitRequestApproveDTO();
            dto.setRequestId(limitRequest.getId());
            dto.setSum(limitRequest.getSum());
            dto.setApprovalState(ApprovalState.APPROVED);
            limitRequest = limitRequestService.approve(dto, limitRequest, author);
        }

        return limitRequestMapper.toV2Dto(limitRequest);
    }

    @Override
    public void editRequest(UUID requestId, EmpLimitRequestV2DTO newData, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var limitRequest = limitRequestService.get(requestId).orElseThrow(() -> new EntityNotFoundException(LimitRequest.class, requestId));
        if (!limitRequest.getAuthor().getUserId().equals(userId)) {
            throw new ActionNotAuthorizedException("Залогиненный пользователь не автор заявки");
        }
        if (limitRequest.getStatus() != LimitRequestStatus.INIT) {
            throw new LimitLogicException("Заявка не может быть изменена: не в состоянии INIT");
        }
        limitRequestMapper.update(limitRequest, newData);
        limitRequestService.save(limitRequest);
    }

    private DepLimit getDepartmentLimit(UUID departmentId, int year, String limitServiceType) {
        var limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentId, year, limitServiceType);
        if (limit != null) {
            return limit;
        } else {
            Department department = departmentService.get(departmentId).orElse(null);
            if (department == null) {
                log.error("ERROR: ReservationService: department not found for id " + departmentId);
                return null;
            }
            if (department.getParentId() != null) {
                return getDepartmentLimit(department.getParentId(), year, limitServiceType);
            } else {
                return null;
            }
        }
    }
}
