package ru.sber.transport.corporate_sync.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.messages.easup.avro.DepartmentHeadData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка синхронизации руководителей подразделений")
class DepartmentHeadSyncImplTest {

    private final DepartmentProvider provider = mock(DepartmentProvider.class);

    private final Sender<Department> sender = mock(Sender.class);

    private final BaseSync<DepartmentHeadData> sync = new DepartmentHeadsSyncImpl(provider, sender);

    @Test
    @DisplayName("Синхронизация")
    void test_sync() {
        var cache = mock(OrganizationsCache.class);
        var data = DepartmentHeadData.newBuilder()
                .setEmployeeId(Instancio.create(String.class))
                .setId(Instancio.create(String.class))
                .setOrganizationId(Instancio.create(String.class))
                .build();

        var current = Instancio.create(Department.class);
        var head = Instancio.create(Employee.class);

        when(cache.get(Department.class, DepartmentFilter.class, data.getId())).thenReturn(Optional.of(current));
        when(cache.get(Employee.class, EmployeeFilter.class, data.getEmployeeId())).thenReturn(Optional.of(head));
        when(provider.save(any())).then(it -> it.getArgument(0));

        sync.doSync(cache, data);

        var departmentCaptor = ArgumentCaptor.forClass(Department.class);
        verify(provider).save(departmentCaptor.capture());

        assertThat(departmentCaptor.getValue().getHeadId()).isEqualTo(head.getId());
        assertThat(departmentCaptor.getValue().getId()).isEqualTo(current.getId());
    }

}