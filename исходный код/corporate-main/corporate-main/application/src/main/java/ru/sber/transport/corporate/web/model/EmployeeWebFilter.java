package ru.sber.transport.corporate.web.model;

import lombok.Builder;
import lombok.Getter;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.web.model.OrgStructureType;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class EmployeeWebFilter implements ru.sber.transport.corporate.business.model.EmployeeFilter {

    private final List<UUID> organizations;

    private final List<UUID> departments;

    private final List<UUID> employees;

    private final String fullName;

    private final String personnelNumber;

    private final String humanReadableId;

    private final Active status;

    private final String mobilePhone;

    private final String email;

    private final OrgStructureType orgStructureType;

}
