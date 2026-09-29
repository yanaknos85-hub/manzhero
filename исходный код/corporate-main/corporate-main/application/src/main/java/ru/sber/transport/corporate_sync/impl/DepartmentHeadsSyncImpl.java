package ru.sber.transport.corporate_sync.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messages.easup.avro.DepartmentHeadData;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DepartmentHeadsSyncImpl extends BaseSync<DepartmentHeadData> {

    private static final Logger log = LoggerFactory.getLogger(DepartmentHeadsSyncImpl.class);

    private final DepartmentProvider provider;

    private final Sender<Department> sender;

    @Override
    protected void doSync(OrganizationsCache organizationsCache, DepartmentHeadData data) {
        final var department = organizationsCache.get(Department.class, DepartmentFilter.class, data.getId())
                .orElseThrow(() -> new EntityNotFoundException(Department.class, data.getId()));
        final var employeeId = data.getEmployeeId();
        final var personnelNumber = Optional.of(employeeId).filter(it -> it.matches("\\d+")).map(Long::parseLong).map(String::valueOf).orElse(employeeId);
        organizationsCache.get(Employee.class, EmployeeFilter.class, personnelNumber)
                .ifPresentOrElse(head -> department.setHeadId(head.getId()), () -> log.warn("Head {} not found for department {}", personnelNumber, department.getSyncId()));
        final var savedDepartment = provider.save(department);
        sender.send(savedDepartment);
        organizationsCache.put(Department.class, savedDepartment.getSyncId(), savedDepartment);
    }
}
