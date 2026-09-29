package ru.sberbank.ditsib.transport.limits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChildLimitDepartmentDTO {
    private UUID id;
    private String code;
    private String departmentName;
    private String humanReadableId;
    private UUID departmentHeadId;
}
