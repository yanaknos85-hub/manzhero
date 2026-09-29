package ru.sber.transport.corporate_sync.impl;

import lombok.*;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.messaging.senders.DepartmentSender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.messages.easup.avro.EndData;

@RequiredArgsConstructor
@Component
public class EndDataSyncImpl extends BaseSync<EndData> {

    private final DepartmentProvider departmentProvider;

    private final DepartmentSender departmentSender;

    @Override
    protected void doSync(OrganizationsCache organizationsCache, EndData data) {
        if (data.getEnd()) {
            final var organizationId = organizationsCache.getOrganization();
            final var headless = departmentProvider.getHeadlessDepartments(organizationId);
            departmentProvider.fillHeads(organizationId);
            departmentProvider.streamAll(headless).forEach(departmentSender::send);
            organizationsCache.clear();
        }
    }

    @Override
    protected String synchronizindLog(String organizationId, String id, EndData data) {
        return "Synchronizing %s finished".formatted(organizationId);
    }
}
