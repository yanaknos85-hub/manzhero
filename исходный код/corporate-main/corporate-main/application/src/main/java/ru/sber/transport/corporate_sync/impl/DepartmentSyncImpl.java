package ru.sber.transport.corporate_sync.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.mappers.MessageMapper;
import ru.sber.transport.messages.easup.avro.DepartmentData;

@RequiredArgsConstructor
@Component
@Getter
public class DepartmentSyncImpl extends OrganizationStructureSync<Department, DepartmentFilter, DepartmentData> {

    private final Provider<Department, DepartmentFilter> provider;

    private final MessageMapper<Department, DepartmentData> mapper;

    private final Sender<Department> sender;

    @Override
    protected String getId(DepartmentData data) {
        return data.getId();
    }

    @Override
    protected Class<Department> getDataClass() {
        return Department.class;
    }

    @Override
    protected Class<DepartmentFilter> getFilterClass() {
        return DepartmentFilter.class;
    }
}
