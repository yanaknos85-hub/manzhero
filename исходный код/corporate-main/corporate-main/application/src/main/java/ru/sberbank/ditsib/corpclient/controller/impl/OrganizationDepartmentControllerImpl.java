package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.OrganizationDepartmentController;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.exceptions.DataConflictException;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of DepartmentController
 */
@RequiredArgsConstructor
@RestController
class OrganizationDepartmentControllerImpl implements OrganizationDepartmentController {

    private final DepartmentService departmentService;

    private final OrganizationService organizationService;

    private final EmployeeService employeeService;

    private final EmployeeSender employeeSender;

    @CheckOrganizationAccess
    @Override
    public DepartmentDTO saveDepartment(
            @Organization UUID organizationId,
            NewDepartmentDTO newDepartment
    ) {
        organizationService.validateOrganizationId(organizationId);

        var departmentList = departmentService.getUpperLevelActiveDepartments(organizationId);
        var department = departmentList.stream().findFirst().orElse(null);
        if (newDepartment.getParent() != null) {
            if (newDepartment.getParent().getId() == null && department != null) {
                throw new DuplicateDataException(Department.class, "parent", newDepartment.getParent().getId());
            } else if (newDepartment.getParent().getId() != null && !departmentService.existsById(newDepartment.getParent().getId())) {
                throw new EntityNotFoundException(Department.class, newDepartment.getParent().getId());
            }
        }
        if (newDepartment.getDepartmentHead() != null && newDepartment.getDepartmentHead().getId() != null &&
                !employeeService.existsById(newDepartment.getDepartmentHead().getId())) {
            throw new EntityNotFoundException(Employee.class, newDepartment.getDepartmentHead().getId());
        }
        return departmentService.saveDepartment(organizationId, newDepartment);

    }

    @CheckOrganizationAccess
    @Override
    public void editDepartment(
            @Organization UUID organizationId,
            UUID departmentId,
            NewDepartmentDTO newData
    ) {
        if (newData.getParent() != null && newData.getParent().getId() != null && !departmentService.existsById(newData.getParent().getId())) {
            throw new EntityNotFoundException(Department.class, newData.getParent().getId());
        }
        if (newData.getDepartmentHead() != null && newData.getDepartmentHead().getId() != null &&
                !employeeService.existsById(newData.getDepartmentHead().getId())) {
            throw new EntityNotFoundException(Employee.class, newData.getDepartmentHead().getId());
        }
        var parentId = newData.getParent() != null ? newData.getParent().getId() : null;
        var loopedId = departmentService.detectLooping(departmentId, parentId);
        if (loopedId.isPresent()) {
            throw new DataConflictException(Department.class, DataConflictException.ConflictType.INFINITE_LOOP,
                    departmentId, "parentId", loopedId.get());
        }
        organizationService.validateOrganizationId(organizationId);
        departmentService.updateDepartment(organizationId, departmentId, newData);
    }

    @CheckOrganizationAccess
    @Override
    public void deleteDepartment(
            @Organization UUID organizationId,
            UUID departmentId
    ) {
        var deleted = getDepartment(organizationId, departmentId);
        deleted.setStatus(DepartmentStatus.INACTIVE);
        departmentService.updateDepartment(organizationId, departmentId, deleted);
        employeeService.deleteEmployeeByDepartmentId(deleted.getId()).forEach(employeeSender::send);
    }

    @CheckOrganizationAccess
    @Override
    public DepartmentDTO getDepartment(
            @Organization UUID organizationId,
            UUID departmentId
    ) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.getDepartmentDto(departmentId);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<DepartmentDTO> getChildrenDepartments(
            @Organization UUID organizationId, UUID departmentId, DepartmentParameters parameters
    ) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.getChildrenDepartments(departmentId, parameters);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<DepartmentSelectDTO> getDepartment(
            @Organization UUID organizationId, Set<UUID> departmentSet, DepartmentParameters parameters
    ) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.getActiveDepartmentsDto(departmentSet, organizationId, parameters, null);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<DepartmentSelectDTO> getDepartments(
            @Organization UUID organizationId, DepartmentParameters parameters, DepartmentProjection projection
    ) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.getActiveDepartmentsDto(organizationId, parameters, projection);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<DepartmentDTO> getAllDepartments(
            @Organization UUID organizationId, DepartmentParameters parameters
    ) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.getAllDepartmentsDto(organizationId, parameters);
    }

    @Override
    public Iterable<DepartmentDTO> getLevelDepartments(UUID organizationId) {
        return departmentService.getLevelDepartments(organizationId);
    }

    @Override
    public CheckFilialFlagResutlDTO checkFilialFlagAll(UUID organizationId) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.checkFilialFlagAll(organizationId);
    }

    @Override
    public boolean checkFilialFlag(UUID organizationId, UUID departmentId) {
        organizationService.validateOrganizationId(organizationId);
        Department department = departmentService.getDepartment(departmentId);
        return departmentService.checkFilialFlag(department);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<DepartmentDTO> getUpperLevelActiveDepartments(@Organization UUID organizationId) {
        organizationService.validateOrganizationId(organizationId);
        return departmentService.getUpperLevelActiveDepartmentsDto(organizationId);
    }

    @Override
    public void editPartial(@Organization UUID organizationId, UUID departmentId, List<PatchData<DepartmentPatchField>> list) {
        organizationService.validateOrganizationId(organizationId);
        var department = getDepartment(organizationId, departmentId);
        for (PatchData<DepartmentPatchField> patchData : list) {
            DepartmentPatchField departmentPatchField = patchData.field();
            if (departmentPatchField.equals(DepartmentPatchField.FILIAL_FLAG)) {
                department.setFilialFlag(Boolean.parseBoolean((String) patchData.value()));
                departmentService.updateDepartment(organizationId, departmentId, department);
            }
        }
    }
}
