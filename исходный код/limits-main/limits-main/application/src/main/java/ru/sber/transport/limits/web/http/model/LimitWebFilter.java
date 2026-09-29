package ru.sber.transport.limits.web.http.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.sber.transport.limits.business.model.Status;
import ru.sber.transport.limits.business.model.Type;

import java.util.UUID;

@Getter
@Builder
@EqualsAndHashCode
public class LimitWebFilter implements ru.sber.transport.limits.business.model.LimitFilter {

    private UUID organizationId;

    private UUID departmentId;

    private UUID employeeId;

    private String parentDepartment;

    private String limitOwner;

    private UUID limitId;

    private UUID parentId;

    private String humanReadableId;

    private UUID requestId;

    private Integer year;

    private Status status;

    private String serviceType;

    private Type type;

}
