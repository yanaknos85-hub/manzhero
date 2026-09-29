package ru.sber.transport.corporate_sync.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.corporate_sync.mappers.MessageMapper;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messages.easup.avro.EmployeeData;

@RequiredArgsConstructor
@Component
@Getter
public class EmployeeSyncImpl extends OrganizationStructureSync<Employee, EmployeeFilter, EmployeeData> {

    private final Provider<Employee, EmployeeFilter> provider;

    private final MessageMapper<Employee, EmployeeData> mapper;

    private final Sender<Employee> sender;

    @Override
    protected String getId(EmployeeData data) {
        var personnelNumber = data.getPersonnelNumber();
        if (personnelNumber.matches("\\d+")) {
            return String.valueOf(Long.parseLong(personnelNumber));
        }
        return personnelNumber;
    }

    @Override
    protected Class<Employee> getDataClass() {
        return Employee.class;
    }

    @Override
    protected Class<EmployeeFilter> getFilterClass() {
        return EmployeeFilter.class;
    }

    @Override
    protected void updateLinks(OrganizationsCache cache, Employee target, EmployeeData source) {
        cache.get(Position.class, PositionFilter.class, source.getPositionId())
                        .ifPresent(position -> target.setPositionId(position.getId()));
        target.setDepartmentId(cache.get(Department.class, DepartmentFilter.class, source.getDepartmentId()).orElseThrow(() -> new EntityNotFoundException(Department.class, source.getDepartmentId())).getId());

        final var personnelNumber = target.getPersonnelNumber();
        if (personnelNumber.matches("\\d+")) {
            target.setPersonnelNumber(String.valueOf(Long.parseLong(personnelNumber)));
        } else {
            target.setPersonnelNumber(personnelNumber);
        }
    }
}
