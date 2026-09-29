package ru.sber.transport.corporate_sync.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.messages.easup.avro.DepartmentParentData;

@Component
@RequiredArgsConstructor
public class DepartmentParentSyncImpl extends BaseSync<DepartmentParentData> {

    private final DepartmentProvider provider;

    private final Sender<Department> sender;

    @Override
    protected void doSync(OrganizationsCache organizationsCache, DepartmentParentData data) {
        var department = organizationsCache.get(Department.class, DepartmentFilter.class, data.getId()).orElseThrow();
        organizationsCache.get(Department.class, DepartmentFilter.class, data.getParentId()).ifPresent(parent -> {
            if (parent.getParentId() == null && parent.getStatus() == Active.INACTIVE) {
                return;
            }
            department.setParentId(parent.getId());
            var savedDepartment = provider.save(department);
            sender.send(savedDepartment);
            organizationsCache.put(Department.class, savedDepartment.getSyncId(), savedDepartment);
        });
    }
}
