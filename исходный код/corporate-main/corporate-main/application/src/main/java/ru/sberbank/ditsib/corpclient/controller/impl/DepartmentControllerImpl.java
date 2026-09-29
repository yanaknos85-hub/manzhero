package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.DepartmentController;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;

import java.util.Set;
import java.util.UUID;

/**
 * Implementation of DepartmentController
 */
@RequiredArgsConstructor
@RestController
class DepartmentControllerImpl implements DepartmentController {

    private final DepartmentService departmentService;

    @Override
    public Iterable<DepartmentSelectDTO> searchDepartments(
            Set<UUID> organizations,
            Set<UUID> departments,
            DepartmentParameters parameters,
            DepartmentProjection projection
    ) {
        return departmentService.getDepartmentsDto(organizations, departments, parameters, projection);
    }
}
