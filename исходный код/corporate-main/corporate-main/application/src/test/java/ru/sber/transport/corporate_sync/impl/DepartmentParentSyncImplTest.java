package ru.sber.transport.corporate_sync.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.messages.easup.avro.DepartmentParentData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка синхронизации родительских подразделений")
class DepartmentParentSyncImplTest {

    private final DepartmentProvider provider = mock(DepartmentProvider.class);

    private final Sender<Department> sender = mock(Sender.class);

    private final BaseSync<DepartmentParentData> sync = new DepartmentParentSyncImpl(provider, sender);

    @Test
    @DisplayName("Синхронизация")
    void test_sync() {
        var cache = mock(OrganizationsCache.class);
        var data = DepartmentParentData.newBuilder()
                .setParentId(Instancio.create(String.class))
                .setId(Instancio.create(String.class))
                .setOrganizationId(Instancio.create(String.class))
                .build();

        var current = Instancio.create(Department.class);
        var parent = Instancio.create(Department.class);

        when(cache.get(Department.class, DepartmentFilter.class, data.getId())).thenReturn(Optional.of(current));
        when(cache.get(Department.class, DepartmentFilter.class, data.getParentId())).thenReturn(Optional.of(parent));
        when(provider.save(any())).then(it -> it.getArgument(0));

        sync.doSync(cache, data);

        var departmentCaptor = ArgumentCaptor.forClass(Department.class);
        verify(provider).save(departmentCaptor.capture());

        assertThat(departmentCaptor.getValue().getParentId()).isEqualTo(parent.getId());
        assertThat(departmentCaptor.getValue().getId()).isEqualTo(current.getId());
    }

    @Test
    void doSync_parentInactive() {
        var cache = mock(OrganizationsCache.class);
        var data = DepartmentParentData.newBuilder()
                .setParentId(Instancio.create(String.class))
                .setId(Instancio.create(String.class))
                .setOrganizationId(Instancio.create(String.class))
                .build();

        var current = Instancio.create(Department.class);
        var parent = Instancio.of(Department.class)
                .set(field(Department::getParentId), null)
                .set(field(Department::getStatus), Active.INACTIVE)
                .create();

        when(cache.get(Department.class, DepartmentFilter.class, data.getId())).thenReturn(Optional.of(current));
        when(cache.get(Department.class, DepartmentFilter.class, data.getParentId())).thenReturn(Optional.of(parent));

        sync.doSync(cache, data);

        verify(provider, never()).save(any());
        verify(sender, never()).send(any());
    }
}