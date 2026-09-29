package ru.sberbank.ditsib.transport.limits.dto.v2;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.model.GetEmployeeDTO;

import java.util.UUID;

/**
 * Data transfer object with data about existing department.
 *
 * @param id ID of department.
 * @param code code of department.
 * @param departmentHead department chief.
 * @param humanReadableId human readable ID.
 * @param name name of department.
 */
public record GetDepartmentV2DTO(
        @NotNull
        UUID id,
        String code,
        String name,
        String humanReadableId,
        GetEmployeeDTO departmentHead
        ) {
}