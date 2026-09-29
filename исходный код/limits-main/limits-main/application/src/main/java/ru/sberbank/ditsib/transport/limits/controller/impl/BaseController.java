package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.model.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
abstract class BaseController {

    private final LimitSharingMapper limitSharingMapper;

    /**
     * Transform limit entity to DTO.
     *
     * @return limit dto.
     */
    protected GetLimitDTO transformLimitEntityToDTO(Limit limit, EmployeeService employeeService) {
        if (limit instanceof DepLimit depLimit) {
            return transformLimitEntityToDTO(depLimit, depLimit.getDepartment(), employeeService);
        } else if (limit instanceof EmpLimit empLimit) {
            return transformLimitEntityToDTO(empLimit, empLimit.getEmployee(), employeeService);
        }
        return null;
    }

    /**
     * Transform limit entity to DTO.
     *
     * @return limit dto.
     */
    protected GetLimitDTO transformLimitEntityToDTO(DepLimit limit, @NonNull Department sourceDep, EmployeeService employeeService) {
        if (limit == null) {
            return null;
        }
        var getLimitDTO = createLimit(limit);
        if (getLimitDTO == null) {
            return null;
        }
        getLimitDTO.setReserve(limit.getReserve());
        getLimitDTO.setEconomy(limit.getEconomy());
        getLimitDTO.setLimitType(LimitType.DEPARTMENT);
        var department = transformDepartmentEntityToDTO(sourceDep, employeeService);
        getLimitDTO.setDepartment(department);
        getLimitDTO.setOwner(department.getDepartmentHead());
        getLimitDTO.setLimitOwner(Optional.ofNullable(getLimitDTO.getOwner()).map(GetEmployeeDTO::getId).orElse(null));
        return getLimitDTO;
    }

    /**
     * Transform limit entity to DTO.
     *
     * @return limit dto.
     */
    protected GetLimitDTO transformLimitEntityToDTO(EmpLimit limit, Employee sourceEmp, EmployeeService employeeService) {
        var getLimitDTO = createLimit(limit);
        if (getLimitDTO == null) {
            return null;
        }
        getLimitDTO.setLimitType(LimitType.EMPLOYEE);
        var employee = transformEmployeeEntityToDTO(sourceEmp);
        getLimitDTO.setEmployee(employee);
        getLimitDTO.setOwner(employee);
        getLimitDTO.setLimitOwner(employee.getId());
        return getLimitDTO;
    }

    @Nullable
    private GetLimitDTO createLimit(Limit limit) {
        if (limit == null) {
            return null;
        }
        GetLimitDTO getLimitDTO = new GetLimitDTO();
        getLimitDTO.setId(limit.getId());
        getLimitDTO.setHumanReadableId(limit.getHumanReadableId());
        getLimitDTO.setCreationTime(limit.getCreationTime());
        getLimitDTO.setYear(limit.getYear());
        getLimitDTO.setSum(limit.getSum());
        getLimitDTO.setLimitSharingType(limit.getLimitSharingType());
        getLimitDTO.setLimitServiceType(limit.getLimitServiceType());
        getLimitDTO.setFinalSharing(limit.isFinalSharing());
        getLimitDTO.setUseThisLimit(limit.isUseThisLimit());
        getLimitDTO.setParentLimitId(limit.getParent() != null ? limit.getParent().getId() : null);
        getLimitDTO.setLimitStatus(limit.getLimitStatus());
        getLimitDTO.setLimitOwner(Optional.ofNullable(getLimitDTO.getOwner()).map(GetEmployeeDTO::getId).orElse(null));
        return getLimitDTO;
    }

    /**
     * Transform department entity to DTO.
     *
     * @return department dto.
     */
    protected GetDepartmentDTO transformDepartmentEntityToDTO(@NonNull Department department, EmployeeService employeeService) {
        GetDepartmentDTO getDepartmentDTO = new GetDepartmentDTO();
        getDepartmentDTO.setId(department.getId());
        getDepartmentDTO.setCode(department.getCode());
        getDepartmentDTO.setDepartmentName(department.getDepartmentName());
        getDepartmentDTO.setHumanReadableId(department.getHumanReadableId());
        if (department.getDepartmentHead() != null) {
            getDepartmentDTO.setDepartmentHead(transformEmployeeEntityToDTO(employeeService.get(department.getDepartmentHead().getId()).orElse(null)));
        }
        return getDepartmentDTO;
    }

    /**
     * Transform employee entity to DTO.
     *
     * @return employee dto.
     */
    protected GetEmployeeDTO transformEmployeeEntityToDTO(Employee employee) {
        GetEmployeeDTO getEmployeeDTO = new GetEmployeeDTO();
        if (employee != null) {
            getEmployeeDTO.setId(employee.getId());
            getEmployeeDTO.setHumanReadableId(employee.getHumanReadableId());
            getEmployeeDTO.setFirstName(employee.getFirstName());
            getEmployeeDTO.setPatronymic(employee.getPatronymic());
            getEmployeeDTO.setLastName(employee.getLastName());
            getEmployeeDTO.setPersonnelNumber(employee.getPersonnelNumber());
            getEmployeeDTO.setPositionId(employee.getPositionId());
            getEmployeeDTO.setOrganizationId(employee.getOrganizationId());
        }
        return getEmployeeDTO;
    }

    protected <T extends Period> List<GetLimitSharingDTO> convertToEnrichedLimitSharingDTOList(List<LimitSharing> limitSharinglist,
                                                                                               LimitSharingPerPeriodService<T> limitSharingPerPeriodService
    ) {
        List<GetLimitSharingDTO> limitSharingDTOList = new ArrayList<>();
        for (LimitSharing limitSharing : limitSharinglist) {
            var getLimitSharingDTO = limitSharingMapper.toDto(limitSharing);

            // set limitSharingPerPeriodDTO for current period
            var limitSharingPerPeriod = limitSharingPerPeriodService.getForDate(limitSharing, LocalDate.now());
            GetLimitSharingPerPeriodDTO limitSharingPerPeriodDTO = limitSharingMapper.toDto(limitSharingPerPeriod);
            getLimitSharingDTO.setSumResharingsYear(BigDecimal.ZERO);
            limitSharingPerPeriodDTO.setSumReservedForCurrentPeriod(BigDecimal.ZERO);
            limitSharingPerPeriodDTO.setSumResharingsPeriod(limitSharingPerPeriod.getAdditionalSum());
            getLimitSharingDTO.setLimitSharingPerPeriodDTO(limitSharingPerPeriodDTO);
            limitSharingDTOList.add(getLimitSharingDTO);
        }
        return limitSharingDTOList;
    }
}
