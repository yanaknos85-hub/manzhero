package ru.sber.transport.corporate_sync.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.messaging.senders.DepartmentSender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.messages.easup.avro.EndData;

import java.util.UUID;

import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка окончания синхронизации")
class EndDataSyncImplTest {

    private final DepartmentProvider departmentProvider = mock(DepartmentProvider.class);

    private final DepartmentSender departmentSender = mock(DepartmentSender.class);

    private final BaseSync<EndData> sync = new EndDataSyncImpl(departmentProvider, departmentSender);

    @Test
    @DisplayName("Синхронизация")
    void test_sync() {
        var cache = mock(OrganizationsCache.class);
        var data = EndData.newBuilder()
                .setEnd(true)
                .build();
        final var organizationId = UUID.randomUUID();

        when(cache.getOrganization()).thenReturn(organizationId);
        sync.doSync(cache, data);

        verify(cache).clear();
    }

}